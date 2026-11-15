{
  "id": "a4bd3a10-94d3-4667-a625-b44a3c44c810",
  "state": "FAILED",
  "repository": "Ruciyingjun/CodeForge",
  "pull_request": 8,
  "error": "GitHub API request failed: <urlopen error [SSL: UNEXPECTED_EOF_WHILE_READING] EOF occurred in violation of protocol (_ssl.c:1016)>",
  "created_at": "2026-11-15T06:23:23.619192+00:00",
  "updated_at": "2026-11-15T06:23:57.895780+00:00",
  "tenant_id": "default",
  "cancel_requested": false,
  "input": {
    "mode": "agentic",
    "shadow": false,
    "source": "github-webhook",
    "diff_url": "https://github.com/Ruciyingjun/CodeForge/pull/8.diff",
    "diff_pending": true,
    "release_lane": "stable"
  },
  "report": null,
  "trace": [
    {
      "step": 1,
      "state": "FAILED",
      "message": "Task entered the dead-letter queue: GitHub API request failed: <urlopen error [SSL: UNEXPECTED_EOF_WHILE_READING] EOF occurred in violation of protocol (_ssl.c:1016)>",
      "created_at": "2026-11-15T06:23:57.895780+00:00"
    }
  ],
  "collaboration": []
