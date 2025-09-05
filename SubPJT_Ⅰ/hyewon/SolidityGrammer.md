# ✅ Solidity 기본 문법
## ✨ 1. 컨트랙트 (Contract) 와 함수 (Function)

- **Contract(컨트랙트)** : 솔리디티 코드의 가장 기본적인 단위로, 블록체인 상에 배포될 하나의 "애플리케이션"을 의미함. 컨트랙트 안에는 데이터(상태 변수)와 그 데이터를 처리하는 로직(함수)이 포함된다.

```json
// SPDX-License-Identifier: GPL-3.0 // <- 소스코드의 라이선스

pragma solidity >=0.8.2 <0.9.0; // <- 솔리디티 버전

/**
 * @title Storage
 * @dev Store & retrieve value in a variable
 * @dev 가장 기본적인 데이터 저장 및 조회 컨트랙트 예제
 * @custom:dev-run-script ./scripts/deploy_with_ethers.ts
 */
contract Storage { // 컨트랙트의 범위 : {} 중괄호 내
    // 1. 상태 변수 (State Variable)
    // 블록체인에 영구적으로 저장되느 데이터
    // 값을 변경 때문에 영구적으로 지원되는 데이터
    uint256 number; 

    // 접근 제어자 지정 가능(external, public, private)

    /**
     * @dev Store value in variable
     * @param num value to store
     */
     // contract 내부에 function을 작성한다.
     // function을 통해서 state variable 을 제어
    function store(uint256 num) public { // save 
        number = num;
    }

    /**
     * @dev Return value 
     * @return value of 'number'
     */
    function retrieve() public view returns (uint256){ // print
        return number;
    }
}
```

## ✨ 자료형
### ◎ 기본형 Primitives
- 논리형 (`bool`) : true / false
- 정수형 (`int`, `uint`) :
    - `unit` : 부호 없는 정수 (0과 양수만 표현), unsigned integer → `uint256`과 동일
    - `int` : 부호 있는 정수 (음수, 0, 양수 표현), signed integer  
    ※ 8비트~256비트까지 8단위로 크기를 명시할 수 있음 (Ex. `uint8`, `int32`) 
- 주소형 (`address`) : 20바이트 크기의 이더리움 주소를 저장 (`0x` 로 시작)
    - `address payable` : 이더리움의 주소를 표현, 이더(ETH)를 받고 전송할 수 있는 특별한 주소 타입 
- 바이트형 (`bytes`) : 
    - `bytes#` : `bytes1` ~ `bytes32` 의 고정 크기 바이트 배열
    - `byte[]` : 동적 크기 바이트 배열. 데이터를 바이트로 표현할 때 사용 -> `string` 보다 가스 효율이 좋을 때가 많음

### ◎ 참조형 (Reference Types)
#### [1] 배열 (Array)
- 동일한 자료형의 데이터를 순서대로 저장하는 자료 구조
    - 고정 배열 : `uint[5] public myFixedArray;` // 크기가 5로 고정 
    - 동적 배열 : `uint[] public myDynamicArray;` // 크기가 변할 수 있음
    - 주요 기능 : 
        - `push()` : 요소 추가
        - `pop()` : 마지막 요소 제거
        - `length` : 길이 확인 
        - `delete` : 요소를 초기값으로 변경
- 함수 내에서 로컬 변수로 배열을 사용하기 위해서는 고정 길이로 선언해야 함

#### [2] 매핑 (Mapping)
- Key-Value 쌍으로 데이터를 저장하는 해시 테이블
    - 선언: `mapping(key자료형 => value자료형) public myMapping;`
    - 예시 : `mapping(address => uint) public balances;` // 각 주소별 잔액 저장
    - 특징: Key에 해당하는 Value를 매우 효율적으로 찾을 수 있지만, Key 목록을 직접 순회(iteration)하는 것은 불가능

#### [3] (Struct)
- 여러 자료형을 하나의 관점으로 묶어서 관리하고자 할 때 선언
    - 선언 밎 사용
    ```json
    struct Todo {
    string text;
    bool completed;
    }

    // 구조체 배열을 선언하여 할 일 목록 관리
    Todo[] public todos;

    function create(string memory _text) public {
        todos.push(Todo({text: _text, completed: false}));
    }
    ```
- 구조체의 Array, Mapping 의 값으로 지정 가능

### ◎ 함수 (Functions)
- 접근 제어자 (Visibility): 함수를 어디서 호출할 수 있는지 정의합니다.
    - public: 어디서든 호출 가능 (내부/외부).
    - external: 컨트랙트 외부에서만 호출 가능. API처럼 외부에 기능을 제공할 때 유용하며, 가스 효율이 더 좋음
    - internal: 컨트랙트 내부, 또는 상속받은 자식 컨트랙트에서만 호출 가능.
    - private: 선언된 컨트랙트 내부에서만 호출 가능 (자식 컨트랙트도 불가).
        
        | | **`private`** | **`internal`** | **`public`** | **`external`** |
        | :--- | :---: | :---: | :---: | :---: |
        | **상태 변수 (State Variables)** | O | O | O | X |
        | **함수 (Functions)** | O | O | O | O |

- 상태 변경성 (State Mutability): 함수가 블록체인 상태를 어떻게 다루는지 명시
    - `view` : 상태를 읽기만 하고 변경하지 않는 함수. 호출 시 가스가 소모되지 않음
    - `pure`: 상태를 읽지도, 변경하지도 않는 함수. 오직 입력된 매개변수만으로 결과를 계산함. 
        - (Ex. `function add(uint a, uint b) pure returns(uint) { return a + b; }`)
    - `payable`: 함수가 호출될 때 이더(ETH)를 받을 수 있도록 함
- 반환 (Returns): 여러 개의 값을 반환할 수 있음
    - `function getInfo() public view returns (uint, bool, address) { ... }`

### ◎ 제어문 (Control Flow)
- **조건문** : `if`, `else if`, `else`를 사용하여 조건에 따라 다른 코드를 실행

- **반복문** : `for`, `while`, `do-while`을 사용하여 특정 코드를 반복 실행

```
🚑 주의: 스마트 컨트랙트에서 반복문은 매우 신중하게 사용해야 함.

반복 횟수가 너무 많으면 블록의 가스 한도(Gas Limit)를 초과하여 트랜잭션이 실패할 수 있음
```

## ✨ 화폐 단위 및 전역 변수
```
화폐 단위: 이더리움의 화폐 단위를 코드에서 직접 사용 가능
```

- `wei` : 가장 작은 단위  
    - ※ 모든 계산은 wei를 기준으로 이루어짐
- `gwei` : `1 gwei == 1e9 wei` (10억 wei)
- `ether` : `1 ether == 1e18 wei` (100경 wei)

<br>

```
주요 전역 변수: 컨트랙트 내에서 블록체인 정보에 접근할 때 사용
```

- `msg.sender` : 현재 함수를 호출한 주소 (가장 중요)
- `msg.value` : 함수 호출 시 함께 전송된 이더(ETH)의 양 (wei 단위)
- `block.timestamp` : 현재 블록이 생성된 시간 (Unix 타임스탬프)
- `tx.origin` : 트랜잭션을 최초로 시작한 외부 소유 계정(EOA)