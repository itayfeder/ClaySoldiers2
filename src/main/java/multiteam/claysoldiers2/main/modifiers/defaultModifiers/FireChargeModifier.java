package multiteam.claysoldiers2.main.modifiers.defaultModifiers;

import multiteam.claysoldiers2.main.entity.claysoldier.ClaySoldierEntity;
import multiteam.claysoldiers2.main.modifiers.modifier.CSModifier;
import net.minecraft.world.item.Item;

import java.awt.*;
import java.util.List;

public class FireChargeModifier extends CSModifier {

    public FireChargeModifier(ModifierType modifierType, Item modifierItem, String modifierName, Color modifierColor, boolean canBeStacked, int stackingLimit, List<CSModifier> incompatibleModifiers) {
        super(modifierType, modifierItem, modifierName, modifierColor, canBeStacked, stackingLimit, incompatibleModifiers);
    }

    @Override
    public void onModifierAttack(ClaySoldierEntity targetSoldier) {

    }

    @Override
    public void onModifierHurt(ClaySoldierEntity thisSoldier, ClaySoldierEntity attackerSoldier) {

    }

    @Override
    public void onModifierTick(ClaySoldierEntity thisSoldier) {

    }
}
