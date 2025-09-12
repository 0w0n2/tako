export interface AuctionCalendarData {
    startDate?: Date
    endDate?: Date
    endTime?: string
  }
  
export interface AuctionNewCalendarProps {
onChange?: (data: AuctionCalendarData) => void
}