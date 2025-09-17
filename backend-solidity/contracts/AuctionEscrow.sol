// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

/**
    @title AuctionEscrow
    @dev 낙찰된 경매 건에 대한 대금을 안전하게 예치(escrow)하고 정산하는 컨트랙트
    [동작 플로우]
    1. 백엔드 서버(Spring)가 (판매자, 구매자, 낙찰가) 정보를 담아 컨트랙트를 배포
    2. 구매자가 낙찰가를 컨트랙트에 입금 (deposit)
    3. 구매자가 상품 수령 후 구매 확정 (confirmReceipt)
    4. 판매자가 예치된 대금을 인출 (releaseFunds)
 */
contract AuctionEscrow {
    // --- 상태 변수 정의 ---
    address public seller; // 판매자 주소
    address public buyer; // 구매자 주소
    uint256 public amount; // 거래액 (낙찰가)

    // --- 거래의 진행 상태 ---
    enum State {
        AwaitingPayment, // 0: 생성 됨 (구매자 입금 대기 중)
        AwaitingConfirmation, // 1: 구매자의 수령 확인 대기 중 (입금 후 배송 중)
        Complete, // 2: 거래 완료 (판매자에게 대금 전송)
        Canceled // 3: 거래 취소 (구매자에게 환불)
    }
    State public currentState; // 현재 거래 상태

    // --- 이벤트 정의 ---
    event Deposited(address indexed buyer, uint256 amount); // 입금 완료 이벤트
    event ReceiptConfirmed(address indexed buyer);
    event FundsReleased(address indexed seller, uint256 amount); // 대금 정산 완료 이벤트
    event Cancled(address indexed canceller); // 거래 취소 이벤트

    // --- 제어자 정의 ---
    modifier onlyBuyer() {
        require(msg.sender == buyer, "Only buyer can call this function.");
        _;
    }

    modifier onlySeller() {
        require(msg.sender == seller, "Only seller can call this function.");
        _;
    }

    modifier inState(State _state) {
        require(
            currentState == _state,
            "This function be called in the current state."
        );
        _;
    }

    /**
        @dev 컨트랙트 생성자. 백엔드 서버가 배포하며 거래 당사자와 금액을 설정.
        @param _seller 판매자의 주소(address)
        @param _buyer 경매 낙찰자(구매자)의 주소(address)
        @param _amount 낙찰 금액
     */
    constructor(address _seller, address _buyer, uint256 _amount) {
        seller = _seller;
        buyer = _buyer;
        amount = _amount; // 컨트랙트 배포와 함께 전송된 ETH를 거래액으로 설정
        currentState = State.AwaitingPayment; // 초기 상태 : 입금 대기
    }

    /**
        @dev [구매자] 낙찰 대금을 컨트랙트에 입금(결제)
     */
    function deposit()
        external
        payable
        onlyBuyer
        inState(State.AwaitingPayment)
    {
        require(msg.value == amount, "Incorrect deposit amount");

        currentState = State.AwaitingConfirmation;
        emit Deposited(buyer, msg.value);
    }

    /**
        @dev [구매자] 상품을 수령했음을 확인
     */
    function confirmReceipt()
        external
        onlyBuyer
        inState(State.AwaitingConfirmation)
    {
        currentState = State.Complete;
        emit ReceiptConfirmed(buyer);
    }

    /**
        @dev [판매자] 예치된 대금을 자신의 지갑으로 인출 (Pull-over-Push 패턴 적용)
     */
    function releaseFunds() external onlySeller inState(State.Complete) {
        // 상태를 먼저 변경하여 Re-entrancy 공격 방지
        (bool success, ) = seller.call{value: amount}("");
        require(success, "Failed to send funds to seller");

        emit FundsReleased(seller, amount);
    }
}
