package co.odisea.utils;

import co.odisea.Shield;
import co.odisea.api.common.utils.TimeUtils.TickTime;
import co.odisea.api.mongodb.models.nickname.Nickname;
import co.odisea.framework.exceptions.postconfigured.InvalidInputException;
import co.odisea.framework.interfaces.IsColored;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NBTItem;
import dev.dbassett.skullcreator.SkullCreator;
import gg.projecteden.api.interfaces.HasUniqueId;
import gg.projecteden.parchment.HasOfflinePlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMetaSpawnEgg;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static co.odisea.api.common.utils.Nullables.isNullOrEmpty;
import static co.odisea.utils.Nullables.isNullOrAir;
import static co.odisea.utils.StringUtils.colorize;

public class ItemBuilder implements Cloneable, Supplier<ItemStack> {
    private ItemStack itemStack;
    private ItemMeta itemMeta;
    @Getter
    private final List<String> lore = new ArrayList<>();
    private boolean doLoreize = true;
    private boolean update;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(CustomMaterial material) {
        this(material, 1);
    }

    public ItemBuilder(CustomModel customModel) {
        this(customModel.getMaterial());
        modelId(customModel.getData());
    }

    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemBuilder(CustomMaterial material, int amount) {
        this(new ItemBuilder(material.getMaterial()).modelId(material.getModelId()).amount(amount));
    }

    public ItemBuilder(ItemBuilder itemBuilder) {
        this(itemBuilder.build());
        this.doLoreize = itemBuilder.doLoreize;
    }

    public ItemBuilder(ItemStack itemStack) {
        this(itemStack, false);
    }

    public ItemBuilder(ItemStack itemStack, boolean update) {
        this.itemStack = update ? itemStack : itemStack.clone();
        this.itemMeta = itemStack.getItemMeta() == null ? null : itemStack.getItemMeta();
        if (itemMeta != null && itemMeta.getLore() != null)
            this.lore.addAll(itemMeta.getLore());
        this.update = update;
    }

    public ItemBuilder material(CustomMaterial customMaterial) {
        String displayName = itemMeta.getDisplayName();

        itemStack.setType(customMaterial.getMaterial());
        modelId(customMaterial.getModelId());
        itemMeta = itemStack.getItemMeta();

        name(displayName);

        return this;
    }

    public ItemBuilder material(Material material) {
        itemStack.setType(material);
        itemMeta = itemStack.getItemMeta();
        return this;
    }

    public Material material() {
        return itemStack.getType();
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder color(ColorType colorType) {
        final Material newMaterial = Objects.requireNonNull(colorType.switchColor(itemStack.getType()), "Could not determine color of " + itemStack.getType());
        itemStack.setType(newMaterial);
        return this;
    }

    @Deprecated
    public ItemBuilder durability(int durability) {
        return durability(Integer.valueOf(durability).shortValue());
    }

    @Deprecated
    public ItemBuilder durability(short durability) {
        itemStack.setDurability(durability);
        return this;
    }

    public ItemBuilder damage(int damage) {
        if (!(itemMeta instanceof Damageable damageable)) throw new UnsupportedOperationException("Cannot apply durability to non-damageable item");
        damageable.setDamage(damage);
        return this;
    }

    private static Component removeItalicIfUnset(Component component) {
        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET)
            component = component.decoration(TextDecoration.ITALIC, false);
        return component;
    }

    private static List<Component> removeItalicIfUnset(ComponentLike... components) {
        return AdventureUtils.asComponentList(components).stream().map(ItemBuilder::removeItalicIfUnset).collect(Collectors.toList());
    }

    private static List<Component> removeItalicIfUnset(List<? extends ComponentLike> components) {
        return AdventureUtils.asComponentList(components).stream().map(ItemBuilder::removeItalicIfUnset).collect(Collectors.toList());
    }

    public @NonNull String name() {
        String displayName = itemMeta.getDisplayName();
        if (Nullables.isNullOrEmpty(displayName))
            displayName = StringUtils.camelCase(material());

        return displayName;
    }

    public ItemBuilder name(@Nullable String displayName) {
        if (displayName == null)
            itemMeta.setDisplayName(null);
        else
            itemMeta.setDisplayName(colorize("&f" + displayName));
        return this;
    }

    public ItemBuilder name(@Nullable ComponentLike componentLike) {
        if (componentLike != null)
            itemMeta.displayName(removeItalicIfUnset(componentLike.asComponent()));
        return this;
    }

    public ItemBuilder resetName() {
        return name((String) null);
    }

    public ItemBuilder resetLore() {
        this.lore.clear();
        this.itemMeta.lore(null);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        return setLore(List.of(lore));
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore.clear();
        if (lore != null)
            this.lore.addAll(lore);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(Collection<String> lore) {
        if (lore != null)
            this.lore.addAll(lore);
        return this;
    }

    public ItemBuilder lore(int line, String text) {
        while (lore.size() < line)
            lore.add("");

        lore.set(line - 1, colorize(text));
        return this;
    }

    public ItemBuilder loreRemove(int line) {
        if (isNullOrEmpty(lore)) throw new InvalidInputException("Item does not have lore");
        if (line - 1 > lore.size()) throw new InvalidInputException("Line " + line + " does not exist");

        lore.remove(line - 1);
        return this;
    }

    // overridden by all string lore
    public ItemBuilder componentLore(ComponentLike... components) {
        itemMeta.lore(removeItalicIfUnset(components));
        return this;
    }

    // overridden by all string lore
    public ItemBuilder componentLore(List<? extends ComponentLike> components) {
        itemMeta.lore(removeItalicIfUnset(components));
        return this;
    }

    public @NotNull List<Component> componentLore() {
        return itemMeta.hasLore() ? Objects.requireNonNull(itemMeta.lore()) : new ArrayList<>();
    }

    public ItemBuilder loreize(boolean doLoreize) {
        this.doLoreize = doLoreize;
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return enchant(enchantment, level, true);
    }

    public ItemBuilder enchantMax(Enchantment enchantment) {
        return enchant(enchantment, enchantment.getMaxLevel(), true);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK)
            ((EnchantmentStorageMeta) itemMeta).addStoredEnchant(enchantment, level, ignoreLevelRestriction);
        else
            itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction);

        return this;
    }

    public ItemBuilder enchantRemove(Enchantment enchantment) {
        itemMeta.removeEnchant(enchantment);
        return this;
    }

    public ItemBuilder enchants(ItemStack item) {
        if (item.getItemMeta() != null)
            item.getItemMeta().getEnchants().forEach((enchant, level) -> itemMeta.addEnchant(enchant, level, true));
        return this;
    }

    public ItemBuilder glow() {
        enchant(Enchantment.ARROW_INFINITE);
        itemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        return glow ? glow() : this;
    }

    public boolean isGlowing() {
        return itemMeta.hasEnchant(Enchantment.ARROW_INFINITE) && itemMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder unbreakable() {
        itemMeta.setUnbreakable(true);
        return this;
    }

    public ItemBuilder itemFlags(ItemFlag... flags) {
        itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder itemFlags(List<ItemFlag> flags) {
        return itemFlags(flags.toArray(ItemFlag[]::new));
    }

    public ItemBuilder itemFlags(ItemFlags flags) {
        return itemFlags(flags.get());
    }

    @AllArgsConstructor
    public enum ItemFlags {
        HIDE_ALL(itemFlag -> itemFlag.name().startsWith("HIDE_")),
        ;

        private final Predicate<ItemFlag> predicate;

        public List<ItemFlag> get() {
            return Arrays.stream(ItemFlag.values()).filter(predicate).toList();
        }
    }

    // Custom meta types

    // Leather armor

    public Color dyeColor() {
        if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta)
            return leatherArmorMeta.getColor();
        return null;
    }

    public ItemBuilder dyeColor(Color color) {
        if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta)
            leatherArmorMeta.setColor(color);
        return this;
    }

    public ItemBuilder dyeColor(IsColored color) {
        return dyeColor(color.colored().getBukkitColor());
    }

    public ItemBuilder dyeColor(String hex) {
        return dyeColor(ColorType.hexToBukkit(hex));
    }

    // Potions

    public ItemBuilder potionType(PotionType potionType) {
        return potionType(potionType, false, false);
    }

    public ItemBuilder potionType(PotionType potionType, boolean extended, boolean upgraded) {
        ((PotionMeta) itemMeta).setBasePotionData(new PotionData(potionType, extended, upgraded));
        return this;
    }

    public ItemBuilder potionEffect(PotionEffectType type) {
        return potionEffect(type, 1, 1);
    }

    public ItemBuilder potionEffect(PotionEffectType type, int seconds) {
        return potionEffect(type, seconds, 1);
    }

    public ItemBuilder potionEffect(PotionEffectType type, int seconds, int amplifier) {
        return potionEffect(new PotionEffectBuilder(type).duration(TickTime.SECOND.x(seconds)).amplifier(amplifier - 1));
    }

    public ItemBuilder potionEffect(PotionEffectBuilder potionEffect) {
        ((PotionMeta) itemMeta).addCustomEffect(potionEffect.build(), true);
        return this;
    }

    public ItemBuilder potionEffect(PotionEffect potionEffect) {
        ((PotionMeta) itemMeta).addCustomEffect(potionEffect, true);
        return this;
    }

    public ItemBuilder potionEffectColor(Color color) {
        ((PotionMeta) itemMeta).setColor(color);
        return this;
    }

    // Fireworks

    public ItemBuilder fireworkPower(int power) {
        ((FireworkMeta) itemMeta).setPower(power);
        return this;
    }

    public ItemBuilder fireworkEffect(FireworkEffect... effect) {
        ((FireworkMeta) itemMeta).addEffects(effect);
        return this;
    }

    // Skulls

    public ItemBuilder skullOwner(HasUniqueId hasUUID) {
        SkullMeta skullMeta = SkinCache.of(hasUUID).getHeadMeta();
        ((SkullMeta) itemMeta).setPlayerProfile(skullMeta.getPlayerProfile());
        return this;
    }

    public ItemBuilder skullOwnerUrl(String url) {
        final ItemStack skull = SkullCreator.itemFromUrl(StringUtils.listLast(url, "/"));
        ((SkullMeta) itemMeta).setPlayerProfile(((SkullMeta) skull.getItemMeta()).getPlayerProfile());
        return this;
    }

    @Deprecated
    public ItemBuilder skullOwner(String name) {
        ((SkullMeta) itemMeta).setOwner(name);
        return this;
    }

    @Deprecated
    public ItemBuilder skullOwnerActual(HasOfflinePlayer offlinePlayer) {
        ((SkullMeta) itemMeta).setOwningPlayer(offlinePlayer.getOfflinePlayer());
        return this;
    }

    public @Nullable OfflinePlayer skullOwner() {
        return ((SkullMeta) itemMeta).getOwningPlayer();
    }

    public @Nullable String skullOwnerName() {
        return ((SkullMeta) itemMeta).getOwner();
    }

    public static ItemBuilder fromHeadId(String id) {
        return new ItemBuilder(Shield.getHeadAPI().getItemHead(id));
    }

    // Banners

    public ItemBuilder pattern(DyeColor color, PatternType pattern) {
        return pattern(new Pattern(color, pattern));
    }

    public ItemBuilder pattern(Pattern pattern) {
        BannerMeta bannerMeta = (BannerMeta) itemMeta;
        bannerMeta.addPattern(pattern);
        return this;
    }

    public ItemBuilder symbolBanner(char character, DyeColor patternDye) {
        return symbolBanner(Symbol.of(character), patternDye);
    }

    public ItemBuilder symbolBanner(SymbolBanner.Symbol symbol, DyeColor patternDye) {
        if (symbol == null)
            return this;

        final ColorType color = Objects.requireNonNull(ColorType.of(itemStack.getType()), "Could not determine color of " + itemStack.getType());
        return symbol.get(this, color.getDyeColor(), patternDye);
    }

    // Maps

    public ItemBuilder mapId(int id) {
        return mapId(id, null);
    }

    public ItemBuilder mapId(int id, MapRenderer renderer) {
        MapMeta mapMeta = (MapMeta) itemMeta;
        mapMeta.setMapId(id);
        MapView view = Bukkit.getServer().getMap(id);
        if (view == null) {
            Shield.log("View for map id " + id + " is null");
        } else if (renderer != null)
            view.addRenderer(renderer);
        mapMeta.setMapView(view);
        return this;
    }

    public int getMapId() {
        MapMeta mapMeta = (MapMeta) itemMeta;
        return mapMeta.getMapId();
    }

    public ItemBuilder createMapView(World world) {
        return createMapView(world, null);
    }

    public ItemBuilder createMapView(World world, MapRenderer renderer) {
        MapMeta mapMeta = (MapMeta) itemMeta;
        MapView view = Bukkit.getServer().createMap(world);
        if (renderer != null)
            view.addRenderer(renderer);
        mapMeta.setMapView(view);
        return this;
    }

    // Shulker Boxes

    public ItemBuilder shulkerBox(List<ItemStack> items) {
        return shulkerBox(items.toArray(ItemStack[]::new));
    }

    public ItemBuilder shulkerBox(ItemBuilder... builders) {
        return shulkerBox(Arrays.stream(builders).map(ItemBuilder::build).toList());
    }

    public ItemBuilder shulkerBox(ItemStack... items) {
        shulkerBox(box -> box.getInventory().setContents(items));
        return this;
    }

    public ItemBuilder shulkerBox(Consumer<ShulkerBox> consumer) {
        BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
        ShulkerBox box = (ShulkerBox) blockStateMeta.getBlockState();
        consumer.accept(box);
        blockStateMeta.setBlockState(box);
        return this;
    }

    public List<@Nullable ItemStack> shulkerBoxContents() {
        BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
        ShulkerBox box = (ShulkerBox) blockStateMeta.getBlockState();
        return Arrays.asList(box.getInventory().getContents());
    }

    public List<@Nullable ItemStack> nonAirShulkerBoxContents() {
        return shulkerBoxContents().stream().filter(Nullables::isNotNullOrAir).collect(Collectors.toList());
    }

    public ItemBuilder clearShulkerBox() {
        return shulkerBox(box -> box.getInventory().clear());
    }

    // Books

    public ItemBuilder bookTitle(String title) {
        return bookTitle(new JsonBuilder(title));
    }

    public ItemBuilder bookTitle(@Nullable ComponentLike title) {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        if (title != null)
            bookMeta.title(title.asComponent());
        return this;
    }

    public ItemBuilder bookAuthor(OfflinePlayer author) {
        return bookAuthor(Nickname.of(author));
    }

    public ItemBuilder bookAuthor(String author) {
        return bookAuthor(new JsonBuilder(author));
    }

    public ItemBuilder bookAuthor(@Nullable ComponentLike author) {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        if (author != null)
            bookMeta.author(author.asComponent());
        return this;
    }

    public ItemBuilder bookPage(int page, String content) {
        return bookPage(page, new JsonBuilder(content));
    }

    public ItemBuilder bookPage(int page, ComponentLike content) {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.page(page, content.asComponent());
        return this;
    }

    public ItemBuilder bookPages(String... pages) {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.addPages(Arrays.stream(pages).map(message -> new JsonBuilder(message).asComponent()).toArray(Component[]::new));
        return this;
    }

    public ItemBuilder bookPages(ComponentLike... pages) {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.addPages(Arrays.stream(pages).map(ComponentLike::asComponent).toArray(Component[]::new));
        return this;
    }

    public ItemBuilder bookPages(List<Component> pages) {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.pages(pages);
        return this;
    }

    public ItemBuilder bookPageRemove(int page) {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        final List<Component> pages = new ArrayList<>(bookMeta.pages());
        pages.remove(page - 1);
        bookMeta.pages(pages);
        return this;
    }

    public ItemBuilder bookGeneration(BookMeta.Generation generation) {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.setGeneration(generation);
        return this;
    }

    public ItemBuilder bookMeta(BookMeta bookMeta) {
        itemMeta = bookMeta;
        return this;
    }

    public String getBookPlainContents() {
        final BookMeta bookMeta = (BookMeta) itemMeta;
        return bookMeta.pages().stream().map(AdventureUtils::asPlainText).collect(Collectors.joining(" "));
    }

    // Entities

    public ItemBuilder axolotl(Axolotl.Variant variant) {
        final AxolotlBucketMeta bucketMeta = (AxolotlBucketMeta) itemMeta;
        bucketMeta.setVariant(variant);
        modelId(variant.ordinal());
        return this;
    }

    public ItemBuilder spawnEgg(EntityType entityType) {
        return material(Material.valueOf(entityType.getKey().getKey().toUpperCase() + "_SPAWN_EGG"));
    }

    @SneakyThrows
    public ItemBuilder spawnEgg(Entity entity) {
        final CompoundTag nbt = (CompoundTag) new NBTEntity(entity).getCompound();

        nbt.remove("Motion");
        nbt.remove("Pos");
        nbt.remove("Rotation");
        nbt.remove("WorldUUIDLeast");
        nbt.remove("WorldUUIDMost");
        nbt.remove("FallDistance");
        nbt.remove("OnGround");

        nbt.putString("Paper.SpawnReason", "SPAWNER_EGG");

        return spawnEgg(entity.getType(), nbt);
    }

    @SneakyThrows
    public ItemBuilder spawnEgg(EntityType entityType, CompoundTag nbt) {
        spawnEgg(entityType);
        final CraftMetaSpawnEgg meta = (CraftMetaSpawnEgg) itemMeta;
        final Field entityTagField = meta.getClass().getDeclaredField("entityTag");
        entityTagField.setAccessible(true);
        entityTagField.set(meta, nbt);
        return this;
    }

    // NBT

    public ItemBuilder nbt(Consumer<NBTItem> consumer) {
        final NBTItem nbtItem = nbtItem();
        consumer.accept(nbtItem);
        itemStack = nbtItem.getItem();
        itemMeta = itemStack.getItemMeta();
        return this;
    }

    @NotNull
    private NBTItem nbtItem() {
        return new NBTItem(build());
    }

    public Rarity rarity() {
        final NBTItem nbtItem = nbtItem();
        if (!nbtItem.hasKey(Rarity.NBT_KEY))
            return Rarity.of(build());

        return Rarity.valueOf(nbtItem.getString(Rarity.NBT_KEY));
    }

    public ItemBuilder rarity(Rarity rarity) {
        return nbt(nbtItem -> Rarity.setNBT(nbtItem, rarity));
    }

    public Condition condition() {
        final NBTItem nbtItem = nbtItem();
        if (!nbtItem.hasKey(Condition.NBT_KEY))
            return Condition.of(build());

        return Condition.valueOf(nbtItem.getString(Condition.NBT_KEY));
    }

    public ItemBuilder attribute(Attribute attribute, AttributeModifier value) {
        itemMeta.addAttributeModifier(attribute, value);
        return this;
    }

    public ItemBuilder condition(Condition condition) {
        return nbt(nbtItem -> Condition.setNBT(nbtItem, condition));
    }

    @AllArgsConstructor
    public enum ItemSetting {
        /**
         * Whether an item can be dropped
         */
        DROPPABLE(true),
        /**
         * Whether an item can be placed in an item frame
         */
        FRAMEABLE(true),
        /**
         * Whether an item can be placed
         */
        PLACEABLE(true),
        /**
         * Whether an item can be stored in containers
         */
        STORABLE(true),
        /**
         * Whether an item can be put in the {@code /trash}
         */
        TRASHABLE(true),
        /**
         * Whether an item can be sold in shops
         */
        TRADEABLE(true) {
            @Override
            public boolean of(ItemBuilder builder, boolean orDefault) {
                if (Backpacks.isBackpack(builder.build()))
                    return false;

                return super.of(builder, orDefault);
            }
        },
        MCMMOABLE(true),
        ;

        private final boolean orDefault;

        public String getKey() {
            return name().toLowerCase();
        }

        public boolean of(ItemBuilder builder, boolean orDefault) {
            NBTItem item = builder.nbtItem();
            if (!item.hasKey(getKey()))
                return orDefault;

            return item.getBoolean(getKey());
        }

        public final boolean of(ItemBuilder builder) {
            return of(builder, orDefault);
        }
    }

    public ItemBuilder setting(ItemSetting setting, boolean value) {
        return nbt(nbt -> nbt.setBoolean(setting.getKey(), value));
    }

    public ItemBuilder unset(ItemSetting setting) {
        return nbt(nbt -> nbt.removeKey(setting.getKey()));
    }

    public boolean is(ItemSetting setting) {
        return setting.of(this);
    }

    public boolean isNot(ItemSetting setting) {
        return !is(setting);
    }

    public boolean is(ItemSetting setting, boolean orDefault) {
        return setting.of(this, orDefault);
    }

    public ItemBuilder undroppable() {
        return setting(ItemSetting.DROPPABLE, false);
    }

    public ItemBuilder unframeable() {
        return setting(ItemSetting.FRAMEABLE, false);
    }

    public ItemBuilder unplaceable() {
        return setting(ItemSetting.PLACEABLE, false);
    }

    public ItemBuilder unstorable() {
        return setting(ItemSetting.STORABLE, false);
    }

    public ItemBuilder untrashable() {
        return setting(ItemSetting.TRASHABLE, false);
    }

    public ItemBuilder untradeable() {
        return setting(ItemSetting.TRADEABLE, false);
    }

    public ItemBuilder modelId(int id) {
        if (id > 0)
            nbt(item -> item.setInteger(CustomModel.NBT_KEY, id));
        return this;
    }

    public int modelId() {
        NBTItem nbtItem = nbtItem();
        final Integer modelId = nbtItem.getInteger(CustomModel.NBT_KEY);
        return modelId == null ? 0 : modelId;
    }

    public static class ModelId {

        public static int of(ItemStack item) {
            if (isNullOrAir(item))
                return 0;

            return of(new ItemBuilder(item));
        }

        public static int of(ItemBuilder item) {
            return item.modelId();
        }

        public static boolean hasModelId(ItemStack item) {
            return of(item) != 0;
        }

        public static boolean hasModelId(ItemBuilder item) {
            return of(item) != 0;
        }

    }

    // Use this when you don't want the glowing, infinite deaths, & no combining
    // TODO ProtocolLib instead?
    public ItemBuilder soulbound() {
        nbt(nbtItem -> nbtItem.setBoolean(SoulboundEnchant.NBT_KEY, true));
        return this;
    }

    // Building //

    @Override
    public ItemStack get() {
        return build();
    }

    public ItemStack build() {
        if (update) {
            buildLore();
            if (itemMeta != null)
                itemStack.setItemMeta(itemMeta);
            return itemStack;
        } else {
            ItemStack result = itemStack.clone();
            buildLore();
            if (itemMeta != null)
                result.setItemMeta(itemMeta);
            return result;
        }
    }

    public void buildLore() {
        if (lore.isEmpty())
            return; // don't override Component lore
        lore.removeIf(Objects::isNull);
        List<String> colorized = new ArrayList<>();
        for (String line : lore)
            if (doLoreize)
                colorized.addAll(StringUtils.loreize(colorize(line)));
            else
                colorized.add(colorize(line));
        itemMeta.setLore(colorized);

        itemStack.setItemMeta(itemMeta);
        itemMeta = itemStack.getItemMeta();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public ItemBuilder clone() {
        itemStack.setItemMeta(itemMeta);
        ItemBuilder builder = new ItemBuilder(itemStack.clone());
        builder.lore(lore);
        builder.loreize(doLoreize);
        return builder;
    }

    /** Static helpers */

    public static ItemBuilder oneOf(ItemStack item) {
        return new ItemBuilder(item).amount(1);
    }

    public static ItemStack setName(ItemStack item, String name) {
        return new ItemBuilder(item, true).name(name).build();
    }

    public static ItemStack setDurability(ItemStack item, double percentage) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            double maxDurability = item.getType().getMaxDurability();
            double damage = (percentage / 100.0) * maxDurability;
            damageable.setDamage((int) damage);

            item.setItemMeta((ItemMeta) damageable);
        }

        return item;
    }

}
