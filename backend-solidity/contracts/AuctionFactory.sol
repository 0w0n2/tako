// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol";
import "@openzeppelin/contracts/proxy/Clones.sol";
import "@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol";
import "./AuctionEscrow.sol";

/**
    @title AuctionFactory
    @dev AuctionEscrow 컨트랙트 인스턴스를 생성하고 추적하는 공장 컨트랙트
 */
contract AuctionFactory is Initializable, OwnableUpgradeable {
    address public implementation; // AuctionEscrow 로직 컨트랙트 주소 (업그레이드를 위해 변경 가능)
    address[] public allEscrows; // 생성된 모든 에스크로 컨트랙트의 주소를 저장

    event EscrowCreated(
        address indexed newEscrowAddress,
        address indexed seller,
        address indexed buyer
    );

    /** 아래 주석 제거하면 X */
    /// @custom:oz-upgrades-unsafe-allow constructor
    constructor() {
        _disableInitializers();
    }

    /**
        @dev 팩토리 초기화 함수
        최초 로직 컨트랙트 주소를 설정하고 배포자를 소유자로 지정
     */
    function initialize(address _implementation) public initializer {
        __Ownable_init(msg.sender);
        implementation = _implementation;
    }

    /**
        @dev 새로운 AuctionEscrow 프록시 컨트랙트 생성
     */
    function createEscrow(
        address _seller,
        address _buyer,
        uint256 _amount
    ) external returns (address) {
        address newEscrow = Clones.clone(implementation);
        AuctionEscrow(newEscrow).initialize(_seller, _buyer, _amount);

        allEscrows.push(newEscrow);
        emit EscrowCreated(newEscrow, _seller, _buyer);

        return newEscrow;
    }

    /**
        @dev [관리자 전용] AuctionEscrow 로직 업그레이드할 경우 새로운 주소 설정
     */
    function setImplementation(address _newImplementation) external onlyOwner {
        implementation = _newImplementation;
    }
}
