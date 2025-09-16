import type { Metadata } from "next";
import { Montserrat } from "next/font/google";
import "./globals.css";
import Header from "@/components/header/Header";
import Footer from "@/components/Footer";
import dynamic from 'next/dynamic'
import TopPadding from "@/components/TopPadding";

const montserrat = Montserrat({
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700", "800", "900"],
  weight: ["300", "400", "500", "600", "700", "800", "900"],
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
  const MosaicReveal = dynamic(() => import('@/components/overlays/MosaicReveal'), { ssr: true })
  const MosaicReveal = dynamic(() => import('@/components/overlays/MosaicReveal'), { ssr: true })
  return (
    <html lang="ko" className="dark">
      <body className={`${montserrat.className}`}>
        <MosaicReveal />
        <MosaicReveal />
        <Header />
        <TopPadding>
          <div>
            {children}
          </div>
        </TopPadding>
        <Footer />
      </body>
    </html>
  );
}
