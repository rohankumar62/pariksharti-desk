'use client';
import {createContext,useContext} from 'react';
import {translator,type Language} from './locale';
export const LocaleContext=createContext<Language>('en');
export function useLocale(){const language=useContext(LocaleContext);return {language,t:translator(language)};}
