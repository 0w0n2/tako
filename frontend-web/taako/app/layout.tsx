// app/layout.tsx
import type { Metadata } from "next";
import { Montserrat } from "next/font/google";
import "./globals.css";
import "@/components/cards/all-cards.css";
import Header from "@/components/header/Header";
import Footer from "@/components/Footer";
import dynamic from "next/dynamic";
import ReactQueryProvider from "@/components/providers/ReactQueryProvider";

const montserrat = Montserrat({
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700", "800", "900"],
});

export const metadata: Metadata = {
  title: "TAKO: TCG Auction Korea",
  description: "Auction TCG cards on blockchain",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  // ✅ MosaicReveal은 CSR 전용이 안전
  const MosaicReveal = dynamic(() => import("@/components/overlays/MosaicReveal"), {
    ssr: false,
    loading: () => null,
  });

  return (
    <html lang="ko" className="dark">
      <body className={montserrat.className}>
        {/* ✅ 최상단에 독립 루트 2개 (상위 리렌더/컨텍스트와 분리) */}
        <div id="overlay-root" />
        <div id="modal-root" />

        <ReactQueryProvider>
          <MosaicReveal />
          <Header />
          <div className="py-30">{children}</div>
          <Footer />
        </ReactQueryProvider>
      </body>
    </html>
  );
}
