// test/AuctionEscrow.test.ts

import { expect } from "chai";
import { ethers } from "hardhat";
import { AuctionEscrow } from "../typechain-types";
import { HardhatEthersSigner } from "@nomicfoundation/hardhat-ethers/signers";

// --- 테스트에 사용될 상수 정의 ---
const CONTRACT_NAME: string = "AuctionEscrow";
const AUCTION_AMOUNT: bigint = ethers.parseEther("1.0"); // 거래 금액, 1 ETH
const INCORRECT_AMOUNT: bigint = ethers.parseEther("0.5"); // 잘못된 입금액

const State = {
  AwaitingPayment: 0,
  AwaitingConfirmation: 1,
  Complete: 2,
  Canceled: 3,
};

describe(`${CONTRACT_NAME} 컨트랙트`, function () {
  // --- 테스트 스코프 전역 변수 선언 ---
  let escrow: AuctionEscrow;
  let seller: HardhatEthersSigner;
  let buyer: HardhatEthersSigner;
  let otherAccount: HardhatEthersSigner;

  // [beforeEach] 각 테스트 실행 전, 컨트랙트를 새로 배포하여 초기화
  beforeEach(async function () {
    // 테스트용 계정(Signer)들을 가져옴
    [seller, buyer, otherAccount] = await ethers.getSigners();

    const AuctionEscrowFactory = await ethers.getContractFactory(CONTRACT_NAME);

    // seller 계정으로 컨트랙트를 배포
    escrow = (await AuctionEscrowFactory.connect(seller).deploy(
      seller.address,
      buyer.address,
      AUCTION_AMOUNT
    )) as AuctionEscrow;
  });

  // --- 1. 배포(Deployment) 테스트 ---
  describe("배포 (Deployment)", function () {
    it("정확한 판매자(seller) 주소로 초기화해야 합니다.", async function () {
      expect(await escrow.seller()).to.equal(seller.address);
    });

    it("정확한 구매자(buyer) 주소로 초기화해야 합니다.", async function () {
      expect(await escrow.buyer()).to.equal(buyer.address);
    });

    it("정확한 거래 금액(amount)으로 초기화해야 합니다.", async function () {
      expect(await escrow.amount()).to.equal(AUCTION_AMOUNT);
    });

    it("초기 상태는 'AwaitingPayment' 이어야 합니다.", async function () {
      expect(await escrow.currentState()).to.equal(State.AwaitingPayment);
    });
  });

  // --- 2. 성공 시나리오 테스트 ---
  describe("성공 시나리오 (Successful Flow)", function () {
    it("입금 → 구매 확정 → 대금 인출까지 전체 흐름이 정상적으로 동작해야 합니다.", async function () {
      // 1. 입금 (deposit)
      // 트랜잭션 실행 후, buyer와 escrow 컨트랙트의 잔액 변경을 검증
      await expect(() =>
        escrow.connect(buyer).deposit({ value: AUCTION_AMOUNT })
      ).to.changeEtherBalances(
        [buyer, escrow],
        [-AUCTION_AMOUNT, AUCTION_AMOUNT]
      );
      // 상태가 AwaitingConfirmation으로 변경되었는지 확인
      expect(await escrow.currentState()).to.equal(State.AwaitingConfirmation);

      // 2. 수령 확인 (confirmReceipt)
      // 'ReceiptConfirmed' 이벤트가 buyer 주소와 함께 정상적으로 발생하는지 검증
      await expect(escrow.connect(buyer).confirmReceipt())
        .to.emit(escrow, "ReceiptConfirmed")
        .withArgs(buyer.address);
      // 상태가 Complete로 변경되었는지 확인
      expect(await escrow.currentState()).to.equal(State.Complete);

      // 3. 대금 인출 (releaseFunds)
      // 트랜잭션 실행 후, seller와 escrow 컨트랙트의 잔액 변경을 검증
      await expect(() =>
        escrow.connect(seller).releaseFunds()
      ).to.changeEtherBalances(
        [seller, escrow],
        [AUCTION_AMOUNT, -AUCTION_AMOUNT]
      );
    });
  });

  // --- 3. 실패 시나리오 테스트 ---
  describe("실패 시나리오 (Revert Scenarios)", function () {
    it("구매자가 아닌 다른 계정이 입금을 시도할 경우 'NotBuyer' 에러와 함께 실패해야 합니다.", async function () {
      // 'revertedWithCustomError'를 통해 특정 커스텀 에러로 실패하는지 검증
      await expect(
        escrow.connect(otherAccount).deposit({ value: AUCTION_AMOUNT })
      ).to.be.revertedWithCustomError(escrow, "NotBuyer");
    });

    it("입금액이 일치하지 않을 경우 'IncorrectAmount' 에러와 함께 실패해야 합니다.", async function () {
      await expect(
        escrow.connect(buyer).deposit({ value: INCORRECT_AMOUNT })
      ).to.be.revertedWithCustomError(escrow, "IncorrectAmount");
    });

    it("구매 확정이 되지 않은 상태에서 판매자가 인출을 시도할 경우 'InvalidState' 에러와 함께 실패해야 합니다.", async function () {
      // 인출 가능한 상태(Complete)가 아니므로 실패해야 함
      await escrow.connect(buyer).deposit({ value: AUCTION_AMOUNT });
      await expect(
        escrow.connect(seller).releaseFunds()
      ).to.be.revertedWithCustomError(escrow, "InvalidState");
    });
  });
});
