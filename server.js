const express = require('express');
const { GoogleGenAI } = require('@google/genai');

const app = express();
app.use(express.json());

// Initialize Gemini Client
const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

// Resilient Fallback Logic: Pro to Flash (503 Protection)
async function callGeminiWithFallback(prompt) {
    const models = ['gemini-2.5-pro', 'gemini-2.5-flash'];
    let lastError = null;

    for (const model of models) {
        let retries = 3;
        let delay = 1000;

        for (let attempt = 1; attempt <= retries; attempt++) {
            try {
                console.log(`[NOBODY ULTRA AI] Trying Model: ${model} (Attempt ${attempt})`);
                const response = await ai.models.generateContent({
                    model: model,
                    contents: prompt,
                });
                return { text: response.text, modelUsed: model };
            } catch (error) {
                lastError = error;
                console.warn(`[NOBODY ULTRA AI] Error on ${model}:`, error.message);
                if (attempt < retries) {
                    await new Promise(resolve => setTimeout(resolve, delay));
                    delay *= 2; // Exponential Backoff
                }
            }
        }
    }
    throw new Error(`All Models Failed: ${lastError.message}`);
}

// Master NOBODY API Endpoint
app.post('/api/nobody-core', async (req, res) => {
    try {
        const { prompt } = req.body;
        if (!prompt) return res.status(400).json({ error: 'Prompt is required' });

        const result = await callGeminiWithFallback(prompt);
        res.json({ success: true, ...result });
    } catch (error) {
        res.status(503).json({ success: false, error: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`NOBODY Ultra AI Server running on port ${PORT}`));
