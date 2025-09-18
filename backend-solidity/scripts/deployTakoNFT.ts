import { ethers, upgrades } from "hardhat";

async function main() {
  const [deployer] = await ethers.getSigners();
  console.log(`Deploying with account: ${deployer.address}`);

  const TakoCardNFTFactory = await ethers.getContractFactory("TakoCardNFT");

  // deployProxy는 로직, 프록시, 관리자 컨트랙트를 한 번에 배포하고 연결해줍니다.
  const takoCardNFT = await upgrades.deployProxy(
    TakoCardNFTFactory,
    [deployer.address], // 생성자가 아닌 Initializer에 전달될 인자
    { initializer: 'initialize' } // 생성자 대신 사용할 초기화 함수 이름
  );

  await takoCardNFT.waitForDeployment();
  const contractAddress = await takoCardNFT.getAddress();
  console.log(`✅ Proxy contract deployed to: ${contractAddress}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});