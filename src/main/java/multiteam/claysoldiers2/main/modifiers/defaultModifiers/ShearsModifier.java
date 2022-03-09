package multiteam.claysoldiers2.main.modifiers.defaultModifiers;

import multiteam.claysoldiers2.main.entity.claysoldier.ClaySoldierEntity;
import multiteam.claysoldiers2.main.modifiers.modifier.CSModifier;
import multiteam.claysoldiers2.main.modifiers.modifier.DamageBonusCSModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;
import oshi.util.tuples.Pair;

import java.awt.*;
import java.util.List;

public class ShearsModifier extends DamageBonusCSModifier {

    boolean canUseShears = false;

    public ShearsModifier(ModifierType modifierType, Item modifierItem, String modifierName, Color modifierColor, List<RegistryObject<CSModifier>> incompatibleModifiers) {
        super(modifierType, modifierItem, modifierName, modifierColor, incompatibleModifiers);
    }

    @Override
    public float getDamageBonus() {
        return canUseShears ? 5 : 0;
    }

    @Override
    public void onModifierAdded(ClaySoldierEntity thisSoldier, Instance thisModifierInstance) {

    }

    @Override
    public void onModifierAttack(ClaySoldierEntity thisSoldier, Entity targetEntity, Instance thisModifierInstance) {

    }

    @Override
    public Pair<DamageSource, Float> onModifierHurt(ClaySoldierEntity thisSoldier, DamageSource damageSource, float damageAmount, Instance thisModifierInstance) {
        return new Pair<>(damageSource, damageAmount);
    }

    @Override
    public void onModifierDeath(DamageSource damageSource, ClaySoldierEntity thisSoldier, Instance thisModifierInstance) {

    }

    @Override
    public void onModifierTick(ClaySoldierEntity thisSoldier, Instance thisModifierInstance) {
        this.canUseShears = thisSoldier.getMainHandItem().is(Items.SHEARS) && thisSoldier.getOffhandItem().is(Items.SHEARS);
    }
}
