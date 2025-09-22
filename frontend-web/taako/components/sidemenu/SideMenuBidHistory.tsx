import {
    Table,
    TableBody,
    TableCaption,
    TableCell,
    TableFooter,
    TableHead,
    TableHeader,
    TableRow,
  } from "@/components/ui/table"

  const invoices = [
    {
      invoice: "INV001",
      paymentStatus: "2025/09/24 10:24",
      totalAmount: "$250.00",
      nickName: "Anonymous",
    },
    {
      invoice: "INV002",
      paymentStatus: "2025/09/25 07:24",
      totalAmount: "$350.00",
      nickName: "Nickname",
    },
  ]

export default function SideMenuBidHistory(){
    return(
        <Table>
          {/* <TableCaption>A list of your recent invoices.</TableCaption> */}
          <TableHeader>
            <TableRow>
              <TableHead className="h-8">입찰시간</TableHead>
              {/* <TableHead className="h-8">닉네임</TableHead> */}
              <TableHead className="h-8 text-right">입찰액</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {invoices.map((invoice) => (
              <TableRow key={invoice.invoice}>
                <TableCell className="text-xs py-2">{invoice.paymentStatus}</TableCell>
                {/* <TableCell className="text-xs py-2">{invoice.nickName}</TableCell> */}
                <TableCell className="text-xs py-2 text-right">{invoice.totalAmount}</TableCell>
              </TableRow>
            ))}
          </TableBody>
          <TableFooter>
            <TableRow>
                <TableCell></TableCell>
                <TableCell className="text-right">내 입찰액: <span className="text-green-400">$350.00</span></TableCell>
            </TableRow>
          </TableFooter>
        </Table>
    )
}