import { Navigate, Route, Routes } from "react-router-dom";

import { HomePage } from "@/features/game/pages/HomePage";
import { LobbyPage } from "@/features/game/pages/LobbyPage";

export function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/lobby/:gameUuid" element={<LobbyPage />} />
      <Route path="*" element={<Navigate replace to="/" />} />
    </Routes>
  );
}

export default App;
