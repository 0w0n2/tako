// SPDX-License-Identifier: MIT
// NFT 발행 후, 각 NFT의 경매 이력을 블록체인에 영구적으로 기록
// 백엔드 서버가 관리자 역할 수행 발행 및 이력 기록 통제하는 중앙 관리형 구조
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract TakoCardNFT is ERC721, Ownable {
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

    // 컨트랙트 생성자(한 번 실행)
    constructor(address initialOwner) ERC721("TakoNFT", "TAKO_RECORD") Ownable(initialOwner) {
        backendAdmin = initialOwner;
    }

    // backendAdmin만 접근 가능
    modifier onlyBackendAdmin() {
        require(msg.sender == backendAdmin, "Not authorized: backend only");
        _;
    }

    // 관리자만 호출 가능한 NFT 발행 함수
    function safeMint(address to, uint256 tokenId) external onlyBackendAdmin {
        _safeMint(to, tokenId);
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
    function getAuctionHistories(uint256 cardId) external view returns (AuctionHistory[] memory) {
        return auctionHistories[cardId];
    }
    
    // 필요한 경우 backendAdmin 주소 변경 함수 (소유자만 가능)
    function setBackendAdmin(address newAdmin) external onlyOwner {
        backendAdmin = newAdmin;
    }
}