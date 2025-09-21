// $ npx hardhat run scripts/getErrorSelectors.ts
import { ethers } from "hardhat";
import fs from "fs";
import path from "path";

// ABI 파일이 있는 경로
const ARTIFACTS_PATH = path.join(__dirname, "../artifacts/contracts");

// 분석할 컨트랙트 이름 목록
const CONTRACT_NAMES = [
  "AuctionEscrow.sol",
  "AuctionFactory.sol",
  "TakoCardNFT.sol",
];

async function main() {
  console.log("Generating Java Map for Custom Error Selectors...\n");

  let javaMapContent =
    "private static final Map<String, String> CUSTOM_ERROR_MAP = Map.of(\n";

  for (const contractName of CONTRACT_NAMES) {
    const artifactPath = path.join(
      ARTIFACTS_PATH,
      contractName,
      `${path.parse(contractName).name}.json`
    );
    if (fs.existsSync(artifactPath)) {
      const artifact = JSON.parse(fs.readFileSync(artifactPath, "utf8"));
      const abi = artifact.abi;

      javaMapContent += `        // --- ${artifact.contractName} Errors ---\n`;

      const errorAbis = abi.filter((entry: any) => entry.type === "error");

      for (const error of errorAbis) {
        // 예: "NotSeller()"
        const signature = `${error.name}(${error.inputs
          .map((i: any) => i.type)
          .join(",")})`;
        // 예: "0x01130a60"
        const selector = ethers.id(signature).substring(0, 10);

        javaMapContent += `        "${selector}", "${error.name}",\n`;
      }
    }
  }
  // 마지막 쉼표 제거 및 Map 닫기
  javaMapContent = javaMapContent.slice(0, -2) + "\n    );";

  console.log(javaMapContent);
  console.log("\nCopy this map into your ContractErrorDecoder.java file.");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
