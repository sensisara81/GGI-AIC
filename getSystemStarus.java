/**
 * Get current system status (public endpoint)
 */
exports.getSystemStatus = functions.https.onRequest(async (req, res) => {
  // Enable CORS
  res.set('Access-Control-Allow-Origin', '*');

  if (req.method === 'OPTIONS') {
    res.set('Access-Control-Allow-Methods', 'GET');
    res.set('Access-Control-Allow-Headers', 'Content-Type');
    res.status(204).send('');
    return;
  }

  try {
    const metricsDoc = await db.collection('system_metrics').doc('current').get();

    if (!metricsDoc.exists) {
      res.status(404).json({ error: 'Metrics not found' });
      return;
    }

    const metrics = metricsDoc.data();

    // Return public-safe subset
    res.json({
      technical: metrics.technical, // Includes ISF, Trust Index, Latency, etc. (Simulated)
      timeline: metrics.timeline,   // Includes daysToCoronation, daysToRatification
      status: metrics.status,       // Includes apolloCIC, redCode, blockchain health
      governance: {
        signatureProgress: metrics.council.signatureProgress, // Percentage signed
        testingActive: metrics.testing.totalVolunteers > 0    // Simple boolean status
      },
      updatedAt: metrics.updatedAtISO
    });

  } catch (error) {
    console.error('Status endpoint error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});
