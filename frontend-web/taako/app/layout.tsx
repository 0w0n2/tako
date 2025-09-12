import type { Metadata } from "next";
import { Montserrat } from "next/font/google";
import "./globals.css";
import Header from "@/components/header/Header";
import Footer from "@/components/Footer";

const montserrat = Montserrat({
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: "TAKO: TCG Auction Korea",
  description: "Auction TCG cards on blockchain",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className="dark">
      <body className={`${montserrat.className}`}>
        <Header />
        <div className="pt-[160px] bg-[#141420]">
          {children}
        </div>
        <Footer />
      </body>
    </html>
  );
}
