import { expect } from "chai";
import { ethers, upgrades } from "hardhat";
import { loadFixture } from "@nomicfoundation/hardhat-network-helpers";
import { AuctionEscrow, AuctionFactory } from "../typechain-types";
import { HardhatEthersSigner } from "@nomicfoundation/hardhat-ethers/signers";

// --- 테스트에 사용될 상수 정의 ---
const FACTORY_CONTRACT_NAME = "AuctionFactory";
const ESCROW_CONTRACT_NAME = "AuctionEscrow";

// --- 테스트 설명 ---
describe(`${FACTORY_CONTRACT_NAME} 컨트랙트 테스트`, function () {
  // --- 상태 Enum 상수 ---
  const State = {
    AwaitingPayment: 0,
    AwaitingConfirmation: 1,
    Complete: 2,
    Canceled: 3,
  };

  /**
   * @dev 테스트 실행 전 초기 상태를 설정하는 Fixture 함수.
   * 모든 컨트랙트를 배포하고 초기화
   */
  async function deployContractsFixture() {
    const [owner, otherAccount] = await ethers.getSigners();

    // 1. AuctionEscrow 로직(설계도) 컨트랙트 배포
    const AuctionEscrowFactory = await ethers.getContractFactory(
      ESCROW_CONTRACT_NAME
    );
    const escrowLogic = (await AuctionEscrowFactory.deploy()) as AuctionEscrow;
    await escrowLogic.waitForDeployment();

    // 2. AuctionFactory 프록시 배포 및 초기화
    const Factory = await ethers.getContractFactory(FACTORY_CONTRACT_NAME);
    const factory = (await upgrades.deployProxy(
      Factory,
      [await escrowLogic.getAddress()],
      { initializer: "initialize" }
    )) as AuctionFactory;
    await factory.waitForDeployment();

    return { factory, escrowLogic, owner, otherAccount };
  }

  // --- 테스트 케이스 시작 ---

  describe("배포 및 초기화", function () {
    it("최초 소유자(owner)가 정상적으로 설정되어야 합니다.", async function () {
      const { factory, owner } = await loadFixture(deployContractsFixture);
      expect(await factory.owner()).to.equal(owner.address);
    });

    it("최초 로직(implementation) 주소가 정상적으로 설정되어야 합니다.", async function () {
      const { factory, escrowLogic } = await loadFixture(
        deployContractsFixture
      );
      expect(await factory.implementation()).to.equal(
        await escrowLogic.getAddress()
      );
    });
  });

  describe("에스크로 생성 (createEscrow)", function () {
    it("새로운 에스크로 컨트랙트를 성공적으로 생성하고, 상태값이 올바르게 초기화되어야 합니다.", async function () {
      const { factory, owner, otherAccount } = await loadFixture(
        deployContractsFixture
      );

      const seller = owner.address;
      const buyer = otherAccount.address;
      const amount = ethers.parseEther("1.5");
      const ESCROW_CREATED_EVENT = "EscrowCreated";

      // createEscrow 함수 호출 및 이벤트 발생 검증
      await expect(factory.createEscrow(seller, buyer, amount)).to.emit(
        factory,
        ESCROW_CREATED_EVENT
      );

      // 생성된 에스크로 주소 검증
      const createdEscrowAddress = await factory.allEscrows(0);
      expect(createdEscrowAddress).to.be.a.properAddress;

      // 생성된 에스크로 컨트랙트의 내부 상태 검증
      const newEscrow = await ethers.getContractAt(
        ESCROW_CONTRACT_NAME,
        createdEscrowAddress
      );
      expect(await newEscrow.seller()).to.equal(seller);
      expect(await newEscrow.buyer()).to.equal(buyer);
      expect(await newEscrow.amount()).to.equal(amount);
      expect(await newEscrow.currentState()).to.equal(State.AwaitingPayment);
    });
  });

  describe("로직 주소 변경 (setImplementation)", function () {
    it("소유자(owner)는 새로운 로직 주소를 설정할 수 있어야 합니다.", async function () {
      const { factory, owner } = await loadFixture(deployContractsFixture);

      // 테스트용 새 로직 컨트랙트(V2) 배포
      const AuctionEscrowV2Factory = await ethers.getContractFactory(
        ESCROW_CONTRACT_NAME
      );
      const newLogic = await AuctionEscrowV2Factory.deploy();
      await newLogic.waitForDeployment();
      const newLogicAddress = await newLogic.getAddress();

      // 소유자 계정으로 로직 주소 변경 함수 호출
      await factory.connect(owner).setImplementation(newLogicAddress);

      // implementation 주소가 새로운 주소로 변경되었는지 확인
      expect(await factory.implementation()).to.equal(newLogicAddress);
    });

    it("소유자가 아닌 계정은 로직 주소를 변경할 수 없어야 합니다.", async function () {
      const { factory, otherAccount } = await loadFixture(
        deployContractsFixture
      );

      const DUMMY_ADDRESS = "0x0000000000000000000000000000000000000001";
      const UNAUTHORIZED_ERROR = "OwnableUnauthorizedAccount";

      // 소유자가 아닌 계정으로 함수 호출 시 지정된 에러와 함께 실패하는지 확인
      await expect(
        factory.connect(otherAccount).setImplementation(DUMMY_ADDRESS)
      )
        .to.be.revertedWithCustomError(factory, UNAUTHORIZED_ERROR)
        .withArgs(otherAccount.address);
    });
  });
});
