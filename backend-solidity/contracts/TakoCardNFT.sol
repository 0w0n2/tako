// SPDX-License-Identifier: MIT
// NFT 발행 후, 각 NFT의 경매 이력을 블록체인에 영구적으로 기록
// 백엔드 서버가 관리자 역할 수행 발행 및 이력 기록 통제하는 중앙 관리형 구조
pragma solidity ^0.8.28;

import "@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol";
import "@openzeppelin/contracts-upgradeable/token/ERC721/ERC721Upgradeable.sol";
import "@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol";
import "@openzeppelin/contracts-upgradeable/proxy/utils/UUPSUpgradeable.sol";

contract TakoCardNFT is Initializable, ERC721Upgradeable, OwnableUpgradeable, UUPSUpgradeable {
    struct AuctionHistory {
        address seller;         // 경매 개시자 지갑 주소
        address buyer;          // 입찰자 지갑 주소
        uint256 price;
        uint256 gradeId;
        uint256 timestamp;
    }

    // 핵심 데이터 구조: tokenId별 경매 이력 배열
    mapping(uint256 => AuctionHistory[]) private auctionHistories;
    // 백엔드 서버의 지갑주소
    address private backendAdmin;

    // --- 클레임 기능 추가 1 : 새로운 데이터 저장 공간 ---
    // 토큰 ID별 시크릿 코드의 해시값을 저장 (보안을 위해 원본이 아닌 해시값 저장)
    mapping(uint256 => bytes32) private tokenSecrets;
    // 한번 사용된 시크릿 코드를 기록하여 재사용 방지
    mapping(bytes32 => bool) private usedSecrets;

    /// @custom:oz-upgrades-unsafe-allow constructor
       constructor() {
        _disableInitializers();
    }

    function initialize(address initialOwner) public initializer {
        // 상속받은 컨트랙트들의 초기화 함수를 호출
        __ERC721_init("TakoNFT", "TAKO_RECORD");
        __Ownable_init(initialOwner);
        __UUPSUpgradeable_init();

        // 기존 constructor의 로직을 그대로 가져옴
        backendAdmin = initialOwner;
    }

    function _authorizeUpgrade(address newImplementation) internal override onlyOwner {}

    // backendAdmin만 접근 가능
    modifier onlyBackendAdmin() {
        require(msg.sender == backendAdmin, "Not authorized: backend only");
        _;
    }

    // 관리자만 호출 가능한 NFT 발행 함수
    function safeMint(address to, uint256 tokenId) external onlyBackendAdmin {
        require(to == backendAdmin, "Can only mint to backend admin");
        _safeMint(to, tokenId);
    }

    // --- 클레임 기능 추가 2: 백엔드용 시크릿 등록 함수 ---
    /**
     * @dev 백엔드 서버가 NFT 발행 후, 해당 NFT를 클레임할 수 있는 시크릿 코드의 해시를 등록합니다.
     * @param tokenId 시크릿을 등록할 NFT의 ID
     * @param secretHash keccak256으로 해시된 시크릿 코드
     */
    function registerSecret(uint256 tokenId, bytes32 secretHash) external onlyBackendAdmin {
        require(ownerOf(tokenId) == backendAdmin, "NFT is not owned by admin");
        tokenSecrets[tokenId] = secretHash;
    }

    // --- 클레임 기능 추가 3: 사용자용 클레임 함수 ---
    /**
     * @dev 사용자가 시크릿 코드를 사용하여 NFT의 소유권을 직접 가져갑니다(claim).
     * @param tokenId 클레임할 NFT의 ID
     * @param secret 사용자가 실물 카드에서 확인한 원본 시크릿 코드 (문자열)
     */
    function claim(uint256 tokenId, string calldata secret) external {
        // 1. NFT가 클레임 가능한 상태인지 확인 (소유주가 admin인지)
        require(ownerOf(tokenId) == backendAdmin, "NFT already claimed or not owned by admin");

        // 2. 사용자가 제출한 시크릿 코드가 유효한지 확인
        bytes32 secretHash = keccak256(abi.encodePacked(secret));
        require(tokenSecrets[tokenId] == secretHash, "Invalid secret code");
        require(!usedSecrets[secretHash], "Secret code already used");

        // 3. 시크릿 코드를 '사용 완료'로 기록 (재사용 방지)
        usedSecrets[secretHash] = true;

        // 4. NFT 소유권을 함수 호출자(사용자)에게 이전
        _transfer(backendAdmin, msg.sender, tokenId);
    }

    // 관리자만 호출 가능한 경매 이력 추가 함수
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
    }

    // cardId별 경매 이력 조회 함수 (읽기용, 누구나 호출 가능)
    // TODO : cardId가 아닌 tokenId로 변경 필요
    function getAuctionHistories(uint256 cardId) external view returns (AuctionHistory[] memory) {
        return auctionHistories[cardId];
    }
    
    // 필요한 경우 backendAdmin 주소 변경 함수 (소유자만 가능)
    function setBackendAdmin(address newAdmin) external onlyOwner {
        backendAdmin = newAdmin;
    }
}