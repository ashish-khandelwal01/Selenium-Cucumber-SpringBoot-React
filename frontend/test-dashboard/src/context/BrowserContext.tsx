import React, { createContext, useContext, useState } from 'react';

const BrowserContext = createContext();

export const useBrowser = () => {
    const context = useContext(BrowserContext);
    if (!context) {
        throw new Error('useBrowser must be used within a BrowserProvider');
    }
    return context;
};

export const BrowserProvider = ({ children }) => {
    const [selectedBrowser, setSelectedBrowser] = useState(
        localStorage.getItem('selectedBrowser') || 'chrome'
    );

    const updateBrowser = (browser) => {
        setSelectedBrowser(browser);
        localStorage.setItem('selectedBrowser', browser);
    };

    return (
        <BrowserContext.Provider value={{
            selectedBrowser,
            updateBrowser
        }}>
            {children}
        </BrowserContext.Provider>
    );
};