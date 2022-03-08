package multiteam.claysoldiers2.main.entity.claysoldier;

import multiteam.claysoldiers2.main.Registration;
import multiteam.claysoldiers2.main.entity.ai.ClaySoldierAttackGoal;
import multiteam.claysoldiers2.main.entity.base.ClayEntityBase;
import multiteam.claysoldiers2.main.item.ModItems;
import multiteam.claysoldiers2.main.modifiers.CSAPI;
import multiteam.claysoldiers2.main.modifiers.modifier.CSModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClaySoldierEntity extends ClayEntityBase {

    private List<CSModifier.Instance> modifiers = new ArrayList<>();

    public ClaySoldierEntity(EntityType<? extends PathfinderMob> entity, Level world, CSAPI.ClaySoldierMaterial material) {
        super(entity, world, material);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0D).add(Attributes.MOVEMENT_SPEED, 0.1F).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.FOLLOW_RANGE, CSAPI.Settings.soldierViewDistance);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ClaySoldierAttackGoal(this, 2, true));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, ClaySoldierEntity.class, 0, true, false, (targetEntity) -> {
            if (targetEntity instanceof ClaySoldierEntity targetedSoldier) {
                return !targetedSoldier.isMatchingMaterial(this);
            }
            return false;
        }));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    @Override
    public ItemStack getItemForm() {
        ItemStack itemForm = new ItemStack(this.getMaterial().getItemForm());

        if (!this.getModifiers().isEmpty()) {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (CSModifier.Instance entry : getModifiers()) {
                CompoundTag modifierTag = new CompoundTag();
                modifierTag.putString("Type", entry.getModifier().getRegistryName().toString());
                modifierTag.putInt("Amount", entry.getAmount());
                list.add(modifierTag);
            }
            tag.put("Modifiers", list);

            itemForm.setTag(tag);
        }

        return itemForm;
    }

    @Override
    public Item getBrickedForm() {
        return ModItems.BRICKED_SOLDIER.get();
    }



    public List<CSModifier.Instance> getModifiers() {
        return this.modifiers;
    }

    public void addModifier(CSModifier.Instance modifierToAdd) {
        this.modifiers.add(modifierToAdd);
    }

    public void removeModifier(CSModifier.Instance modifierToRemove) {
        this.modifiers.remove(modifierToRemove);
    }



    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag data) {
        super.addAdditionalSaveData(data);

        ListTag list = new ListTag();
        for (CSModifier.Instance entry : this.getModifiers()) {
            Integer amount = entry.getAmount();
            ResourceLocation modifier = entry.getModifier().getRegistryName();
            CompoundTag modifierTag = new CompoundTag();
            modifierTag.putString("Type", Objects.requireNonNull(modifier).toString());
            modifierTag.putInt("Amount", amount);
            list.add(modifierTag);
        }
        data.put("Modifiers", list);

    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag data) {
        super.readAdditionalSaveData(data);

        // Data structure:
        // + "Modifier" (Compound)
        // |-  "Type" (String)
        // |-  "Amount" (Int)
        for (Tag tag : data.getList("Modifiers", Tag.TAG_STRING)) {
            if (tag instanceof CompoundTag modifierTag) {
                ResourceLocation type = new ResourceLocation(modifierTag.getString("Type"));
                CSModifier modifier = Registration.getModifierRegistry().getValue(type);
                int amount = modifierTag.getInt("Amount");
                this.addModifier(new CSModifier.Instance(modifier, amount));
            }
        }

    }



    @Override
    public void tick() {
        super.tick();
        ClaySoldierEntity soldier = this;
        Level level = soldier.getLevel();

        //Picking up items
        if (!level.isClientSide) {

            List<ItemEntity> itemsAround = level.getEntitiesOfClass(ItemEntity.class, new AABB(soldier.getX() - 1, soldier.getY() - 1, soldier.getZ() - 1, soldier.getX() + 1, soldier.getY() + 1, soldier.getZ() + 1));

            for (ItemEntity itemEntity : itemsAround) {
                Pair<CSModifier.Instance, Integer> pickUpModifier = shouldPickUp(itemEntity.getItem());

                if(pickUpModifier.getB() > 0 && this.getModifiers().contains(pickUpModifier.getA())){
                    int indexOfOldModifier = this.getModifiers().indexOf(pickUpModifier.getA());
                    CSModifier.Instance oldModifier = this.getModifiers().get(indexOfOldModifier);

                    int newAmount = oldModifier.getAmount()+pickUpModifier.getB();

                    this.getModifiers().set(indexOfOldModifier, new CSModifier.Instance(oldModifier.getModifier(), newAmount));

                    itemEntity.getItem().shrink(pickUpModifier.getB());
                }

            }
        }

        //Calling on Modifier Tick
        if (!level.isClientSide && !this.getModifiers().isEmpty()) {
            for (int i = 0; i < this.getModifiers().size(); i++) {
                if (this.getModifiers().get(i) != null) {
                    this.getModifiers().get(i).getModifier().onModifierTick(soldier);
                } else {
                    return;
                }
            }
        }

    }

    public Pair<CSModifier.Instance, Integer> shouldPickUp(ItemStack stack) {
        int pickUpAmount = 0;
        CSModifier.Instance retInstance = null;

        for (CSModifier modifier : Registration.getModifierRegistry().getValues()) {
            if (modifier.getModifierItem() == stack.getItem()) {
                CSModifier.Instance thisModifierInstance = null;
                for (CSModifier.Instance inst : this.getModifiers()) {
                    if(inst.getModifier() == modifier){
                        thisModifierInstance = inst;
                        break;
                    }
                }

                boolean hasIncompatibles = false;
                if (!modifier.getIncompatibleModifiers().isEmpty()) {
                    for (CSModifier.Instance modif : this.getModifiers()) {
                        if (modifier.getIncompatibleModifiers().contains(modif.getModifier())) {
                            hasIncompatibles = true;
                        }
                    }
                }

                if (!hasIncompatibles) {
                    if (this.getModifiers().contains(modifier)){
                        if(modifier.canBeStacked()){
                            retInstance = thisModifierInstance;
                            pickUpAmount = modifier.getMaxStackingLimit()-thisModifierInstance.getAmount();
                            break;
                        }
                    }else{
                        if(modifier.canBeStacked()){
                            retInstance = thisModifierInstance;
                            pickUpAmount = modifier.getMaxStackingLimit()-thisModifierInstance.getAmount();
                            break;
                        }else{
                            retInstance = thisModifierInstance;
                            pickUpAmount = 1;
                            break;
                        }
                    }
                }

            }
        }
        return new Pair<>(retInstance, pickUpAmount);
    }

}
