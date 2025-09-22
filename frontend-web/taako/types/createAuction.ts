export interface AuctionNewCalendarProps {
  onChange?: (data: {
    startDate?: Date
    startTime?: string
    endDate?: Date
    endTime?: string
  }) => void
}