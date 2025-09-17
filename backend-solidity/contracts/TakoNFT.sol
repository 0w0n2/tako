// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract TakoCardNFT is ERC721, Ownable {
    struct AuctionHistory {
        address bidder;     // 입찰자
        string uuid;           // 입찰자 고유 아이디
        string cardid;         // 카드 아이디
        uint256 bidAmount;  // 입찰 금액
        uint256 timestamp;  // 입찰 시간
        // string txHash;        // 거래 해시
        // string cardimage;     // 카드 이미지
        // string cardname;      // 카드 이름
    }

    mapping(uint256 => AuctionHistory[]) private auctionHistories;
    
    address private backendAdmin;

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
        uint256 cardId,
        address bidder,
        string memory uuid,
        string memory cardid,
        uint256 bidAmount,
        uint256 timestamp
    ) external onlyBackendAdmin {
        auctionHistories[cardId].push(AuctionHistory({
            bidder: bidder,
            uuid: uuid,
            cardid: cardid,
            bidAmount: bidAmount,
            timestamp: timestamp
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