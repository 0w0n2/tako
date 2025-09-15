// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

/**
 * @title AuctionEscrow
 * @dev 낙찰된 경매 건에 대한 대금을 안전하게 예치(escrow)하고 정산하는 컨트랙트
 * [동작 플로우]
 * 1. 백엔드 서버(Spring)가 (판매자, 구매자, 낙찰가) 정보를 담아 컨트랙트를 배포
 * 2. 구매자가 낙찰가를 컨트랙트에 입금 (deposit)
 * 3. 구매자가 상품 수령 후 구매 확정 (confirmReceipt)
 * 4. 판매자가 예치된 대금을 인출 (releaseFunds)
 *
 */
contract AuctionEscrow {
    /** 상태 변수 정의 */
    address public seller; // 판매자 주소
    address public buyer; // 구매자 주소
    uint256 public amount; // 거래액 (낙찰가)

    // 거래의 진행 상태를 나타내는 Enum
    enum State {
        Created, // 0: 생성됨 (아직 입금 전)
        Locked, // 1: 입금 완료됨 (상품 배송 중)
        Release, // 2: 구매 확정됨 (판매자가 인출 가능)
        Inactive // 3: 거래 종료 (정산 완료 또는 취소/환불)
    }
    State public currentState;

    /** 이벤트 정의 */
    event Deposited(address indexed buyer, uint256 amount);
    event ReceiptConfirmed(address indexed buyer);
    event FundsReleased(address indexed seller, uint256 amount);
    event Cancled(address indexed canceller);

    /** 함수 정의 */

    /**
     * @dev 컨트랙트 생성자. 배포 시점에 거래의 기본 정보를 설정함
     * @param _buyer 경매 낙찰자(구매자)의 주소(address)
     */
    constructor(address _buyer) payable {
        seller = msg.sender; // 컨트랙트를 배포한 주소(백엔드 서버 -> 판매자)
        buyer = _buyer;
        amount = msg.value; // 컨트랙트 배포와 함께 전송된 ETH를 거래액으로 설정
        currentState = State.Created; // 초기 상태 : Created(생성됨)
    }

    /**
     * @dev 구매자가 낙찰 대금을 컨트랙트에 입금(결제)
     * Hardhat v2.22.0 버전에선 constructor 에 payable을 사용할 수 없으므로 별도의 deposit 함수를 만들어 구매자가 직접 호출하게 구현함
     *
     *
     */
    constructor(address _buyer, uint256 _amount) {
        seller = msg.sender;
        buyer = _buyer;
        amount = _amount;
        currentState = State.Created;
    }

    function deposit() external payable {
        require(msg.sender == buyer, "Only buyer can deposit");
        require(
            currentState == State.Created,
            "Deposit already made or auction inactive"
        );
        require(msg.value == amount, "Incorrect deposit amount");

        currentState = State.Locked;
        emit Deposited(buyer, msg.value);
    }

    function confirmReceipt() external {
        require(msg.sender == buyer, "Only buyer can confirm receipt");
        require(currentState == State.Created, "Funds must be deposited first");

        currentState = State.Release;
        emit ReceiptConfirmed(buyer);
    }
}
