import { ethers, upgrades } from "hardhat";

async function main() {
  const [deployer] = await ethers.getSigners();
  console.log(`Deploying with account: ${deployer.address}`);

  const TakoCardNFTFactory = await ethers.getContractFactory("TakoCardNFT");

  const takoCardNFT = await upgrades.deployProxy(
    TakoCardNFTFactory,
    [deployer.address], 
    // UUPS 종류의 프록시를 사용한다고 명시해줍니다.
    { initializer: 'initialize', kind: 'uups' } 
  );

  await takoCardNFT.waitForDeployment();
  const contractAddress = await takoCardNFT.getAddress();
  console.log(`✅ Proxy contract deployed to: ${contractAddress}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});