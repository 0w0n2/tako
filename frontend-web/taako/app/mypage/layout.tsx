import MypageSideMenu from "@/components/sidemenu/MypageSideMenu";

export default function RootLayout({
    children,
  }: Readonly<{
    children: React.ReactNode;
  }>) {
    return (
        <div className="default-container flex pb-40">
            <MypageSideMenu/>
            <div className="flex-1">
              {children}
            </div>
        </div>
    );
}