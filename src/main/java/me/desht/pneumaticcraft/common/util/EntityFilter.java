package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.ImmutableSet;
import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetString;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityFilter implements Predicate<Entity>, com.google.common.base.Predicate<Entity> {
    private static final Pattern ELEMENT_DIVIDER = Pattern.compile(";");
    private static final Pattern ELEMENT_SUBDIVIDER = Pattern.compile("[(),]");

    private static final EntityFilter ALLOW_FILTER = EntityFilter.allow();
    private static final EntityFilter DENY_FILTER = EntityFilter.deny();

    private final List<EntityMatcher> matchers = new ArrayList<>();
    private final boolean sense;
    private final String rawFilter;

    public EntityFilter(String filter) {
        if (filter.startsWith("!")) {
            filter = filter.substring(1);
            sense = false;
        } else {
            sense = true;
        }

        rawFilter = filter;

        Arrays.stream(ELEMENT_DIVIDER.split(filter)).map(EntityMatcher::new).forEach(matchers::add);
    }

    public static EntityFilter fromProgWidget(IProgWidget widget, boolean whitelist) {
        IProgWidget w = widget.getConnectedParameters()[whitelist ? 1 : widget.getParameters().length + 1];
        List<String> l = new ArrayList<>();
        if (w instanceof ProgWidgetString) {
            while (w instanceof ProgWidgetString) {
                String str = ((ProgWidgetString) w).string;
                Validate.isTrue(!str.startsWith("!"), "'!' negation can't be used here (put blacklist filters on left of widget)");
                l.add(str);
                w = w.getConnectedParameters()[0];
            }
            return new EntityFilter(Strings.join(l, ";"));
        } else {
            return whitelist ? ALLOW_FILTER : DENY_FILTER;
        }
    }

    public static EntityFilter fromString(String s) {
        try {
            return new EntityFilter(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static EntityFilter allow() {
        return new ConstantEntityFilter(true);
    }

    public static EntityFilter deny() {
        return new ConstantEntityFilter(false);
    }

    @Override
    public String toString() {
        return rawFilter;
    }

    @Override
    public boolean apply(@Nullable Entity input) {
        return test(input);
    }

    @Override
    public boolean test(Entity entity) {
        for (EntityMatcher m : matchers) {
            if (m.test(entity)) return sense;
        }
        return !sense;
    }

    private enum Modifier {
        AGE(ImmutableSet.of("adult", "baby")),
        BREEDABLE(ImmutableSet.of("yes", "no"));

        private final Set<String> vals;

        Modifier(ImmutableSet<String> v) {
            vals = v;
        }

        boolean isValid(String s) {
            return vals.contains(s);
        }
    }


    private class EntityMatcher implements Predicate<Entity> {
        private final Pattern regex;
        private final Class<?> typeClass;
        private final List<Pair<Modifier,String>> modifiers = new ArrayList<>();

        private EntityMatcher(String element) {

            String[] splits = ELEMENT_SUBDIVIDER.split(element);
            for (int i = 0; i < splits.length; i++) {
                splits[i] = splits[i].trim();
            }

            if (splits[0].startsWith("@")) {
                String sub = splits[0].substring(1);
                if (StringUtils.countMatches(element, "(") != StringUtils.countMatches(element, ")")) {
                    throw new IllegalArgumentException("Mismatched opening/closing braces");
                }
                typeClass = getClassFor(sub);
                regex = null;
            } else {
                typeClass = null;
                regex = Pattern.compile(wildcardToRegex(splits[0]), Pattern.CASE_INSENSITIVE);
            }

            for (int i = 1; i < splits.length; i++) {
                String[] modifier = splits[i].split("=");
                Validate.isTrue(modifier.length == 2, "Invalid modifier syntax: " + splits[i]);
                Modifier m;
                try {
                    m = Modifier.valueOf(modifier[0].toUpperCase());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unknown modifier: " + modifier[0]);
                }
                Validate.isTrue(m.isValid(modifier[1]), "'" + modifier[1] + "' is not a valid value for modifier '" + modifier[0] + "'.  Valid values are: " + Strings.join(m.vals, ","));
                modifiers.add(Pair.of(m, modifier[1]));
            }
        }

        @Override
        public boolean test(Entity entity) {
            boolean ok = false;
            if (typeClass != null) {
                ok = typeClass.isAssignableFrom(entity.getClass());
            } else if (regex != null) {
                Matcher m = regex.matcher(entity.getName());
                ok = m.matches();
            }
            return ok && matchModifiers(entity);
        }

        private boolean matchModifiers(Entity entity) {
            for (Pair<Modifier,String> pair : modifiers) {
                Modifier modifier = pair.getLeft();
                String val = pair.getRight();
                boolean ret = false;
                switch (modifier) {
                    case AGE:
                        if (entity instanceof EntityAgeable) {
                            ret = ((EntityAgeable) entity).getGrowingAge() >= 0 ?
                                    val.equalsIgnoreCase("adult") : val.equalsIgnoreCase("baby");
                        }
                        break;
                    case BREEDABLE:
                        if (entity instanceof EntityAnimal) {
                            ret = ((EntityAnimal) entity).getGrowingAge() == 0 ?
                                    val.equalsIgnoreCase("yes") : val.equalsIgnoreCase("no");
                        }
                        break;
                }
                if (!ret) return false;
            }
            return true;
        }

        private Class<?> getClassFor(String substring) {
            Class<?> typeClass;
            switch (substring) {
                case "mob":
                    typeClass = IMob.class;  // IMob matches some hostile creatures that EntityMob doesn't
                    break;
                case "animal":
                    typeClass = EntityAnimal.class;
                    break;
                case "living":
                    typeClass = EntityLivingBase.class;
                    break;
                case "player":
                    typeClass = EntityPlayer.class;
                    break;
                case "item":
                    typeClass = EntityItem.class;
                    break;
                case "minecart":
                    typeClass = EntityMinecart.class;
                    break;
                case "drone":
                    typeClass = EntityDrone.class;
                    break;
                case "boat":
                    typeClass = EntityBoat.class;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown entity type specifier: @" + substring);
            }
            return typeClass;
        }
    }

    private static String wildcardToRegex(String wildcard) {
        StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\").append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return (s.toString());
    }

    public static class ConstantEntityFilter extends EntityFilter {
        private final boolean allow;

        private ConstantEntityFilter(boolean allow) {
            super("");
            this.allow = allow;
        }

        @Override
        public boolean test(Entity entity) {
            return allow;
        }
    }
}
