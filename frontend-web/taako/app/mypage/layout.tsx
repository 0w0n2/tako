import MypageSideMenu from "@/components/sidemenu/MypageSideMenu";

export default function RootLayout({
    children,
  }: Readonly<{
    children: React.ReactNode;
  }>) {
    return (
        <div>
            <MypageSideMenu/>
            {children}
        </div>
    );
}