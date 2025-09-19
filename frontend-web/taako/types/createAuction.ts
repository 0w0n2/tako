export interface AuctionCalendarData {
    endDate?: Date
    endTime?: string
  }
  
export interface AuctionNewCalendarProps {
  onChange?: (data: AuctionCalendarData) => void
}