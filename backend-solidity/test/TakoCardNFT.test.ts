import { ethers } from "hardhat";
import { expect } from "chai";
import { HardhatEthersSigner } from "@nomicfoundation/hardhat-ethers/signers";
import { TakoCardNFT } from "../typechain-types"; // TypeChain으로 생성된 타입

describe("TakoNFT", function () {
  // 테스트에 사용할 변수들을 선언합니다.
  let takoCardNFT: TakoCardNFT;
  let owner: HardhatEthersSigner;
  let backendAdmin: HardhatEthersSigner;
  let user1: HardhatEthersSigner;
  let user2: HardhatEthersSigner;

  // 각 테스트 케이스 실행 전에 항상 실행되는 부분입니다.
  // 컨트랙트를 새로 배포하여 테스트 환경을 초기화합니다.
  beforeEach(async function () {
    // 테스트용 계정들을 가져옵니다.
    [owner, backendAdmin, user1, user2] = await ethers.getSigners();

    // 컨트랙트 배포
    const TakoCardNFTFactory = await ethers.getContractFactory("TakoCardNFT", owner);
    takoCardNFT = await TakoCardNFTFactory.deploy(owner.address);
    
    // 배포 후, 실제 운영 환경처럼 backendAdmin을 다른 계정으로 설정합니다.
    await takoCardNFT.connect(owner).setBackendAdmin(backendAdmin.address);
  });

  describe("배포 (Deployment)", function () {
    it("Owner와 BackendAdmin이 올바르게 설정되어야 합니다.", async function () {
      expect(await takoCardNFT.owner()).to.equal(owner.address);
      // private 변수는 직접 조회가 안되므로, 테스트를 위한 public getter를 만들거나
      // 이벤트를 통해 검증하는 것이 좋습니다. 여기서는 다른 함수 호출로 역할을 검증합니다.
    });
  });

  describe("NFT 발행 (safeMint)", function () {
    it("BackendAdmin은 NFT를 성공적으로 발행할 수 있어야 합니다.", async function () {
      const tokenId = 1;
      // backendAdmin 계정으로 safeMint 함수 호출
      await expect(takoCardNFT.connect(backendAdmin).safeMint(user1.address, tokenId))
        .to.emit(takoCardNFT, "Transfer") // Transfer 이벤트 발생 확인
        .withArgs(ethers.ZeroAddress, user1.address, tokenId); // 이벤트 인자 검증

      // 발행된 NFT의 소유자가 user1인지 확인
      expect(await takoCardNFT.ownerOf(tokenId)).to.equal(user1.address);
    });

    it("BackendAdmin이 아닌 계정은 NFT 발행에 실패해야 합니다.", async function () {
      const tokenId = 1;
      // owner나 user1 계정으로 safeMint 호출 시 실패해야 함
      await expect(
        takoCardNFT.connect(user1).safeMint(user1.address, tokenId)
      ).to.be.revertedWith("Not authorized: backend only");

      await expect(
        takoCardNFT.connect(owner).safeMint(user1.address, tokenId)
      ).to.be.revertedWith("Not authorized: backend only");
    });
  });

  describe("경매 이력 관리 (Auction History)", function () {
    const tokenId = 1;

    beforeEach(async function () {
      // 이력 테스트를 위해 먼저 NFT를 발행해 둡니다.
      await takoCardNFT.connect(backendAdmin).safeMint(user1.address, tokenId);
    });

    it("BackendAdmin은 경매 이력을 성공적으로 추가할 수 있어야 합니다.", async function () {
      const price = ethers.parseEther("1.0"); // 1 ETH
      // backendAdmin으로 이력 추가
      await takoCardNFT.connect(backendAdmin).addAuctionHistory(tokenId, user1.address, user2.address, price, 1);

      const histories = await takoCardNFT.getAuctionHistories(tokenId);
      expect(histories.length).to.equal(1);
      expect(histories[0].seller).to.equal(user1.address);
      expect(histories[0].buyer).to.equal(user2.address);
      expect(histories[0].price).to.equal(price);
    });

    // it("존재하지 않는 NFT에 경매 이력을 추가하면 실패해야 합니다. (개선안 적용 시)", async function () {
    //   const nonExistentTokenId = 999;
    //   const price = ethers.parseEther("1.0");

    //   // _requireMinted가 추가된 컨트랙트라면 이 테스트는 통과합니다.
    //   await expect(
    //     takoCardNFT.connect(backendAdmin).addAuctionHistory(nonExistentTokenId, user1.address, user2.address, price, 1)
    //   ).to.be.revertedWith("ERC721: invalid token ID");
    // });
  });
});