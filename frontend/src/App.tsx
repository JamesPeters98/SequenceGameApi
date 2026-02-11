import { Navigate, Route, Routes } from "react-router-dom";

import { ThemeProvider } from "@/components/theme-provider";
import { HomePage } from "@/features/game/pages/HomePage";
import { LobbyPage } from "@/features/game/pages/LobbyPage";

export function App() {
  return (
    <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/lobby/:gameUuid/:privatePlayerUuid" element={<LobbyPage />} />
        <Route path="/lobby/:gameUuid" element={<LobbyPage />} />
        <Route path="*" element={<Navigate replace to="/" />} />
      </Routes>
    </ThemeProvider>
  );
}

export default App;
