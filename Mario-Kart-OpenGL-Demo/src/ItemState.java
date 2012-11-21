
public enum ItemState
{
	NO_ITEM,
	THREE_ORBITING_GREEN_SHELLS,
	TWO_ORBITING_GREEN_SHELLS,
	ONE_ORBITING_GREEN_SHELL,
	HOLDING_GREEN_SHELL,
	THREE_ORBITING_RED_SHELLS,
	TWO_ORBITING_RED_SHELLS,
	ONE_ORBITING_RED_SHELL,
	HOLDING_RED_SHELL,
	ONE_MUSHROOM,
	TWO_MUSHROOMS,
	THREE_MUSHROOMS,
	GOLDEN_MUSHROOM,
	FAKE_ITEM_BOX,
	HOLDING_BANANA,
	ONE_BANANA,
	TWO_BANANAS,
	THREE_BANANAS,
	LIGHTNING_BOLT,
	POWER_STAR,
	BOO,
	BLUE_SHELL;
	
	public static ItemState get(int itemID)
	{
		switch(itemID)
		{
			case  0: return HOLDING_GREEN_SHELL;
			case  1: return THREE_ORBITING_GREEN_SHELLS;
			case  2: return HOLDING_RED_SHELL;
			case  3: return THREE_ORBITING_RED_SHELLS;
			case  4: return ONE_MUSHROOM;
			case  5: return THREE_MUSHROOMS;
			case  6: return GOLDEN_MUSHROOM;
			case  7: return FAKE_ITEM_BOX;
			case  8: return HOLDING_BANANA;
			case  9: return THREE_BANANAS;
			case 10: return LIGHTNING_BOLT;
			case 11: return POWER_STAR;
			case 12: return BOO;
			case 13: return BLUE_SHELL;
			default: return NO_ITEM;
		}
	}
	
	public static ItemState press(ItemState state)
	{
		switch(state)
		{
			case THREE_ORBITING_GREEN_SHELLS: return TWO_ORBITING_GREEN_SHELLS;
			case TWO_ORBITING_GREEN_SHELLS:   return ONE_ORBITING_GREEN_SHELL;
			case THREE_ORBITING_RED_SHELLS:   return TWO_ORBITING_RED_SHELLS;
			case TWO_ORBITING_RED_SHELLS:     return ONE_ORBITING_RED_SHELL;
			case THREE_MUSHROOMS:             return TWO_MUSHROOMS;
			case TWO_MUSHROOMS:				  return ONE_MUSHROOM;
			case THREE_BANANAS:				  return TWO_BANANAS;
			case TWO_BANANAS:				  return ONE_BANANA;
			
			case GOLDEN_MUSHROOM:             return GOLDEN_MUSHROOM;			  
			
			case FAKE_ITEM_BOX:               return FAKE_ITEM_BOX;
			case HOLDING_GREEN_SHELL:         return HOLDING_GREEN_SHELL;
			case HOLDING_RED_SHELL:           return HOLDING_RED_SHELL;
			case HOLDING_BANANA:			  return HOLDING_BANANA;
			
			default:                          return NO_ITEM;
		}
	}
	
	public static ItemState release(ItemState state)
	{
		switch(state)
		{
			case HOLDING_GREEN_SHELL:
			case HOLDING_RED_SHELL:
			case FAKE_ITEM_BOX:
			case HOLDING_BANANA:
				
			return NO_ITEM;
			
			default: return state;
		}
	}
	
	public static boolean isInstantUse(ItemState state)
	{
		switch(state)
		{
			case ONE_MUSHROOM:
			case THREE_MUSHROOMS:
			case GOLDEN_MUSHROOM:
			case LIGHTNING_BOLT:
			case POWER_STAR:
			case BOO:
			case BLUE_SHELL:
			
			return true;
			
			default: return false;
		}
	}
	
	public static boolean isMultipleUse(ItemState state)
	{
		switch(state)
		{
			case THREE_MUSHROOMS:
			case TWO_MUSHROOMS:
			case GOLDEN_MUSHROOM:
			
			return true;
				
			default: return false;
		}
	}
	
	public static boolean isTimed(ItemState state)
	{
		switch(state)
		{
			case GOLDEN_MUSHROOM: return true;
				
			default: return false;
		}
	}
}
