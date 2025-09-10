export default function BidInputForm(){
    return(
        <div>
            <form>
                <div>
                    <input
                        type="number"
                        placeholder="입찰금액을 입력해주세요"
                    />
                </div>
                <button type="submit">입찰하기</button>
            </form>
            <p>입찰 단위: {}</p>
        </div>
    )
}