export default function Pagenation(){
    return (
        <div className="flex gap-5 justify-center">
            <button className="text-[#a5a5a5]">{`<`}</button>
            <ul className="flex gap-4">
                <li>1</li>
                <li className="text-[#a5a5a5]">2</li>
                <li className="text-[#a5a5a5]">3</li>
                <li className="text-[#a5a5a5]">4</li>
            </ul>
            <button className="text-[#a5a5a5]">{`>`}</button>
        </div>
    )
}