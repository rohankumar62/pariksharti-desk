import {createContext, useContext} from 'react';
export type Profile = {id: string; email: string; fullName: string; city: string; bio: string; targetYear: number | null; createdAt: string; medium: 'en' | 'hi'};
type AccountContext = {user: Profile; openProfile: () => void; signOut: () => Promise<void>};
export const AuthContext = createContext<AccountContext | null>(null);
export function useAccount() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('Sign in to open your desk.');
  return context;
}
