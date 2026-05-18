import os
import json
import urllib.request
import gzip

# 1. Dynamically figure out paths relative to this script's location
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))  # root/resources/python
RESOURCES_DIR = os.path.dirname(SCRIPT_DIR)              # root/resources
OUTPUT_DIR = os.path.join(RESOURCES_DIR, "json")         # root/resources/json
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "aws_instances_eu_central_1.json")

# Ensure the target directory exists before writing to it
os.makedirs(OUTPUT_DIR, exist_ok=True)

# The live production endpoint hosted by Vantage
DATA_URL = "https://instances.vantage.sh/instances.json"

print("Downloading live data from Vantage source...")

req = urllib.request.Request(
    DATA_URL, 
    headers={
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
        'Accept-Encoding': 'gzip'
    }
)

try:
    response = urllib.request.urlopen(req)
    
    if response.info().get('Content-Encoding') == 'gzip':
        raw_bytes = gzip.decompress(response.read())
    else:
        raw_bytes = response.read()
        
    raw_data = json.loads(raw_bytes.decode('utf-8'))

    filtered_instances = []
    target_region = "eu-central-1"

    for instance in raw_data:
        instance_type = instance.get("instance_type", "unknown")
        vcpus = instance.get("vcpus", 0)
        memory = instance.get("memory", 0)
        storage = instance.get("storage", {}).get("size", "EBS only") if instance.get("storage") else "EBS only"
        network = instance.get("network_performance", "Unknown")
        
        raw_pricing = instance.get("pricing", {})
        
        if target_region not in raw_pricing:
            continue
            
        region_platforms = raw_pricing[target_region]
        pricing_payload = {}
        
        for platform, details in region_platforms.items():
            if "linux" in platform.lower():
                pricing_payload["linux"] = {
                    "on_demand": float(details.get("ondemand", 0)),
                    "reserved_1yr_no_upfront": float(details.get("reserved", {}).get("yrTerm1Standard.noUpfront", 0))
                }
            elif "mswin" in platform.lower():
                pricing_payload["windows"] = {
                    "on_demand": float(details.get("ondemand", 0)),
                    "reserved_1yr_no_upfront": float(details.get("reserved", {}).get("yrTerm1Standard.noUpfront", 0))
                }
                
        if not pricing_payload:
            continue

        filtered_instances.append({
            "instance_type": instance_type,
            "resources": {
                "vcpu": vcpus,
                "memory_gb": memory,
                "storage": storage,
                "network_performance": network
            },
            "pricing": {
                target_region: pricing_payload
            }
        })

    # Save output to root/resources/json/aws_instances_eu_central_1.json
    with open(OUTPUT_FILE, "w") as f:
        json.dump(filtered_instances, f, indent=2)

    print(f"Success! Generated JSON with {len(filtered_instances)} Frankfurt records.")
    print(f"Saved to: {OUTPUT_FILE}")

except urllib.error.HTTPError as e:
    print(f"HTTP Error occurred: {e.code} - {e.reason}")
except Exception as e:
    print(f"An unexpected error occurred: {e}")