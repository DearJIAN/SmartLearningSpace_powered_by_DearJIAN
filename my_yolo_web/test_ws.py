import asyncio
import websockets
import traceback

async def test():
    try:
        async with websockets.connect("wss://echo.websocket.events", additional_headers={"X-Test": "1"}) as ws:
            pass
    except Exception as e:
        traceback.print_exc()

asyncio.run(test())
