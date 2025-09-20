// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol";
import "@openzeppelin/contracts/proxy/Clones.sol";
import "@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol";
import "./AuctionEscrow.sol";
import "@openzeppelin/contracts-upgradeable/proxy/utils/UUPSUpgradeable.sol";

contract AuctionFactory is Initializable, OwnableUpgradeable, UUPSUpgradeable {
    address public implementation;
    address[] public allEscrows;

    event EscrowCreated(
        address indexed newEscrowAddress,
        address indexed seller,
        address indexed buyer,
        uint256 tokenId
    );

    /// @custom:oz-upgrades-unsafe-allow constructor
    constructor() {
        _disableInitializers();
    }

    function initialize(address _implementation) public initializer {
        __Ownable_init(msg.sender); // OwnableUpgradeable v5.0+ 에서는 인자가 없습니다.
        __UUPSUpgradeable_init();
        implementation = _implementation;
    }

    // --- 4. _authorizeUpgrade 함수 추가 ---
    // 이 컨트랙트의 owner만이 업그레이드를 승인할 수 있도록 설정
    function _authorizeUpgrade(address newImplementation) internal override onlyOwner {}

    /**
     * @dev 새로운 AuctionEscrow 프록시 컨트랙트 생성 (수정된 버전)
     */
    function createEscrow(
        address _seller,
        address _buyer,
        uint256 _amount,
        address _takoNFTAddress, // NFT 컨트랙트 주소 추가
        uint256 _tokenId        // NFT 토큰 ID 추가
    ) external returns (address) {
        // OwnableUpgradeable v5.0+ 에서는 onlyOwner만 호출하도록 제어하는 것이 좋습니다.
        // require(msg.sender == owner(), "Not authorized");

        address newEscrow = Clones.clone(implementation);
        // 5개의 인자를 모두 전달하여 initialize 함수 호출
        AuctionEscrow(newEscrow).initialize(_seller, _buyer, _amount, _takoNFTAddress, _tokenId);

        allEscrows.push(newEscrow);
        emit EscrowCreated(newEscrow, _seller, _buyer, _tokenId);

        return newEscrow;
    }

    function setImplementation(address _newImplementation) external onlyOwner {
        implementation = _newImplementation;
    }
}