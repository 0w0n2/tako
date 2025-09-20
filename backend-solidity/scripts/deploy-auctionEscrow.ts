import { ethers } from "hardhat";

async function main() {
  console.log("새로운 AuctionEscrow 로직 컨트랙트(V2)를 배포합니다...");

  const AuctionEscrowV2Factory = await ethers.getContractFactory("AuctionEscrow");
  const escrowLogicV2 = await AuctionEscrowV2Factory.deploy();
  await escrowLogicV2.waitForDeployment();
  const escrowLogicV2Address = await escrowLogicV2.getAddress();

  console.log(`✅ AuctionEscrow V2 [로직] 배포 완료: ${escrowLogicV2Address}`);
  console.log("---");
  console.log("다음 단계: 이 주소를 AuctionFactory의 setImplementation 함수에 전달하세요.");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});