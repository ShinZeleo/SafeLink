
apy-key: dc80db5a68344ca2ae6854e6ee727f58d59fbd962243be9db40d3a6b06ff66e3

# VirusTotal API

## Main API
VirusTotal Public API

Website:
https://www.virustotal.com/

Documentation:
https://docs.virustotal.com/reference/overview

---

## API Purpose
Digunakan untuk:
- URL reputation checking
- phishing detection
- malicious URL validation

---

## API Limits
- 4 lookups / minute
- 500 lookups / day
- 15.5K lookups / month

---

## Request Strategy
- request hanya saat tombol Scan ditekan
- menggunakan SQLite cache
- tidak melakukan auto request
- menggunakan local heuristic detection sebelum API call

---

## Main Detection Flow
1. Local heuristic check
2. SQLite cache check
3. VirusTotal verification
4. Save result to SQLite

---

## Main Endpoint
POST https://www.virustotal.com/api/v3/urls

GET https://www.virustotal.com/api/v3/analyses/{id}

---

## Main Security Logic
SAFE:
- harmless dominant

SUSPICIOUS:
- suspicious > 0

DANGEROUS:
- malicious >= threshold