"use client";

import * as React from "react";
import { ChevronDownIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";

import { AuctionNewCalendarProps } from "@/types/createAuction";

export default function AuctionNewCalendar({ onChange }: AuctionNewCalendarProps) {
  const [endOpen, setEndOpen] = React.useState(false);
  const [endDate, setEndDate] = React.useState<Date | undefined>(undefined);
  const [endTime, setEndTime] = React.useState("10:30:00");

  React.useEffect(() => {
    onChange?.({ endDate, endTime });
  }, [endDate, endTime, onChange]);

  return (
    <div className="flex gap-4 flex-wrap md:flex-nowrap">
      <div className="flex flex-col gap-3">
        <Label htmlFor="date-picker-end" className="text-sm px-1">
          경매 종료 날짜
        </Label>
        <Popover open={endOpen} onOpenChange={setEndOpen}>
          <PopoverTrigger asChild>
            <Button
              variant="outline"
              id="date-picker-end"
              className="w-35 h-[50px] justify-between text-[#a5a5a5]"
            >
              {endDate ? endDate.toLocaleDateString() : "종료 날짜 선택"}
              <ChevronDownIcon />
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-[300px] overflow-hidden p-0 z-91" align="start">
            <Calendar
              mode="single"
              selected={endDate}
              captionLayout="dropdown"
              onSelect={(date) => {
                setEndDate(date);
                setEndOpen(false);
              }}
              disabled={(date) =>
                date < new Date() || date < new Date("1900-01-01")
              }
            />
          </PopoverContent>
        </Popover>
      </div>

      {/* 시간 선택 */}
      <div className="flex flex-col gap-3">
        <Label htmlFor="time-picker" className="text-sm px-1">
          종료 시간
        </Label>
        <Input
          type="time"
          id="time-picker"
          step="1"
          value={endTime}
          onChange={(e) => setEndTime(e.target.value)}
          className="h-[50px] bg-[#191924] appearance-none [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-calendar-picker-indicator]:appearance-none"
        />
      </div>
    </div>
  );
}