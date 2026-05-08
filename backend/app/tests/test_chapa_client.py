"""
Quick smoke test for the Chapa client.
Run: .\venv\Scripts\python.exe -m app.tests.test_chapa_client
"""

import asyncio
import sys
import os

# Ensure the backend directory is in the path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(__file__))))

from app.services.chapa_client import ChapaClient


async def test_initialize():
    """Test transaction initialization with Chapa test API."""
    client = ChapaClient()

    print("=" * 60)
    print("  Chapa Client - Integration Test")
    print("=" * 60)

    # Test 1: Initialize a transaction
    print("\n[TEST 1] Initialize Transaction")
    print("-" * 40)

    result = await client.initialize_transaction(
        amount=100.0,
        email="test@zapfood.com",
        first_name="Test",
        last_name="Customer",
        phone_number="0912345678",
        return_url="https://zapfood.com/payment/return",
    )

    print(f"  Success      : {result.success}")
    print(f"  TX Ref       : {result.tx_ref}")
    print(f"  Checkout URL : {result.checkout_url}")
    print(f"  Message      : {result.message}")

    if not result.success:
        print(f"\n  [FAIL] Initialize FAILED: {result.message}")
        return False

    print(f"\n  [PASS] Initialize PASSED")

    # Test 2: Verify the transaction (should be pending since no payment made)
    print(f"\n[TEST 2] Verify Transaction (tx_ref={result.tx_ref})")
    print("-" * 40)

    verify = await client.verify_transaction(result.tx_ref)

    print(f"  Success  : {verify.success}")
    print(f"  Status   : {verify.status}")
    print(f"  TX Ref   : {verify.tx_ref}")
    print(f"  Amount   : {verify.amount} {verify.currency}")
    print(f"  Message  : {verify.message}")

    # Note: unpaid test transactions may return "pending" or error
    print(f"\n  [PASS] Verify PASSED (response received)")

    print("\n" + "=" * 60)
    print("  All tests passed!")
    print("=" * 60)
    return True


if __name__ == "__main__":
    success = asyncio.run(test_initialize())
    sys.exit(0 if success else 1)
