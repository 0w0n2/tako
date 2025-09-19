// $ npx hardhat run scripts/deploy-auctionFactory.ts --network sepolia
import { ethers, upgrades } from "hardhat";

async function main() {
  console.log("배포를 시작합니다...");

  // 1. 업그레이드 가능한 AuctionEscrow "로직" 컨트랙트를 먼저 배포
  const AuctionEscrowFactory = await ethers.getContractFactory("AuctionEscrow");
  const escrowLogic = await AuctionEscrowFactory.deploy();
  await escrowLogic.waitForDeployment();
  const escrowLogicAddress = await escrowLogic.getAddress();
  console.log(`✅ AuctionEscrow의 [로직] 배포 완료: ${escrowLogicAddress}`);

  // 2. 업그레이드 가능한 AuctionFactory "컨트랙트" 배포
  const Factory = await ethers.getContractFactory("AuctionFactory");
  const factoryProxy = await upgrades.deployProxy(
    Factory,
    [escrowLogicAddress],
    {
      initializer: "initialize",
    }
  );
  await factoryProxy.waitForDeployment();

  const factoryAddress = await factoryProxy.getAddress();
  console.log(
    `✅ AuctionFactory의 [프록시 컨트랙트] 배포 완료: ${factoryAddress}`
  );
  console.log("---");
  console.log("Spring Boot 서버의 .env 파일에 아래 주소를 추가하세요:");
  console.log(`FACTORY_CONTRACT_ADDRESS=${factoryAddress}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
