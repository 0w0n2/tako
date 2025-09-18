// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

// V1과 동일한 업그레이드 가능 버전의 컨트랙트들을 임포트합니다.
import "@openzeppelin/contracts-upgradeable/token/ERC721/ERC721Upgradeable.sol";
import "@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol";
import "@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol";
import "@openzeppelin/contracts-upgradeable/proxy/utils/UUPSUpgradeable.sol";

// --- V2 변경사항 1: 컨트랙트 이름 변경 ---
contract TakoCardNFT_V2 is Initializable, ERC721Upgradeable, OwnableUpgradeable, UUPSUpgradeable {
    struct AuctionHistory {
        address seller;
        address buyer;
        uint256 price;
        uint256 gradeId;
        uint256 timestamp;
    }

    mapping(uint256 => AuctionHistory[]) private auctionHistories;
    address private backendAdmin;

    // --- V2 변경사항 2: 새로운 이벤트 추가 ---
    event HistoryAddedV2(uint256 indexed tokenId, address buyer);

    /// @custom:oz-upgrades-unsafe-allow constructor
    constructor() {
        _disableInitializers();
    }

    function initialize(address initialOwner) public initializer {
        __ERC721_init("TakoNFT", "TAKO_RECORD");
        __Ownable_init(initialOwner);
        __UUPSUpgradeable_init();
        backendAdmin = initialOwner;
    }

    // --- V2 변경사항 3: 새로운 함수 추가 ---
    function version() public pure returns (string memory) {
        return "V2";
    }

    function _authorizeUpgrade(address newImplementation) internal override onlyOwner {}

    modifier onlyBackendAdmin() {
        require(msg.sender == backendAdmin, "Not authorized: backend only");
        _;
    }

    function safeMint(address to, uint256 tokenId) external onlyBackendAdmin {
        _safeMint(to, tokenId);
    }

    // --- V2 변경사항 4: 기존 함수 수정 ---
    function addAuctionHistory(
        uint256 tokenId,
        address seller,
        address buyer,
        uint256 price,
        uint256 gradeId
    ) external onlyBackendAdmin {
        auctionHistories[tokenId].push(AuctionHistory({
            seller: seller,
            buyer: buyer,
            price: price,
            gradeId: gradeId,
            timestamp: block.timestamp
        }));

        // V2에서 추가된 이벤트 발생
        emit HistoryAddedV2(tokenId, buyer);
    }

    function getAuctionHistories(uint256 cardId) external view returns (AuctionHistory[] memory) {
        return auctionHistories[cardId];
    }
    
    function setBackendAdmin(address newAdmin) external onlyOwner {
        backendAdmin = newAdmin;
    }
}