import asyncio
import json
import httpx
import sys
from datetime import datetime

BASE_URL = "http://localhost:8000"

async def test_ai_health():
    print("\n--- 1. Checking AI Service Health ---")
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(f"{BASE_URL}/")
            if response.status_code == 200:
                print("✅ Backend is UP")
            else:
                print(f"❌ Backend returned {response.status_code}")
        except Exception as e:
            print(f"❌ Failed to connect to backend: {e}")

async def test_ai_recommendations():
    print("\n--- 2. Testing AI Personalized Recommendations (Gemini) ---")
    url = f"{BASE_URL}/api/recommendations/ai/user123"
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            response = await client.get(url)
            if response.status_code == 200:
                data = response.json()
                recs = data.get("recommendations", [])
                print(f"✅ Received {len(recs)} recommendations")
                if recs:
                    print(f"   First Rec: {recs[0].get('mealName')} - {recs[0].get('reason')}")
                print(f"✅ AI Reasoning: {data.get('reasoning')[:100]}...")
            else:
                print(f"❌ API Error {response.status_code}: {response.text}")
        except Exception as e:
            print(f"❌ AI Recommendation request failed: {e}")

async def test_behavior_tracking():
    print("\n--- 3. Testing Behavioral Tracking API ---")
    url = f"{BASE_URL}/api/analytics/track"
    event = {
        "id": "test_event_" + str(int(datetime.now().timestamp())),
        "userId": "user123",
        "mealId": "meal_shiro_001",
        "interactionType": "VIEW",
        "timestamp": int(datetime.now().timestamp() * 1000),
        "sessionId": "session_test",
        "deviceType": "script",
        "mealCategory": "Traditional",
        "fastingRelevant": True,
        "metadata": {"isFastingChoice": True}
    }
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(url, json=event)
            if response.status_code == 200:
                print("✅ Behavior event tracked successfully")
            else:
                print(f"❌ Tracking failed {response.status_code}")
        except Exception as e:
            print(f"❌ Behavior tracking request failed: {e}")

async def test_hybrid_logic():
    print("\n--- 4. Testing Hybrid Scoring (Rule-Based + AI) ---")
    # This hits the personalized endpoint which uses the internal RecommendationEngine
    url = f"{BASE_URL}/api/ai/recommendations"
    payload = {
        "userId": "user123",
        "mealTime": "Lunch",
        "currentDay": "Wednesday",
        "fastingMode": True
    }
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(url, json=payload)
            if response.status_code == 200:
                data = response.json()
                recs = data.get("recommendedMeals", [])
                print(f"✅ Received {len(recs)} hybrid recommendations")
                # Check if fasting rule is respected
                non_fasting = [r for r in recs if "Meat" in r.get("reason", "")]
                if not non_fasting:
                    print("✅ Fasting rule respected (No meat in results)")
                else:
                    print("⚠️ Fasting rule might be loose in reasoning")
            else:
                print(f"❌ Hybrid API failed {response.status_code}")
        except Exception as e:
            print(f"❌ Hybrid request failed: {e}")

async def main():
    print("==================================================")
    print("      ZapFood AI System Verification Tool")
    print("==================================================")
    
    await test_ai_health()
    await test_behavior_tracking()
    # Wait a moment for Firestore to catch up
    await asyncio.sleep(1)
    await test_ai_recommendations()
    await test_hybrid_logic()
    
    print("\n==================================================")
    print("      Verification Complete")
    print("==================================================")

if __name__ == "__main__":
    asyncio.run(main())
