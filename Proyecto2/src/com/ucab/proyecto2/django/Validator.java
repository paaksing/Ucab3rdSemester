package com.ucab.proyecto2.django;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class Validator<M extends Manager<T>, T extends Model> {

    @SuppressWarnings("serial")
    public static class ValidationError extends Exception {
        public ValidationError(String errorMessage) {
            super(errorMessage);
        }
    }

    @SuppressWarnings("serial")
    public static class UniqueConstraintFailed extends ValidationError { 
        public <E, M extends Manager<T>, T extends Model> UniqueConstraintFailed(M owner, String key, E value) {
            super(owner.getClass().getName() + "." + key + ": " + value.toString());
        }
    }

    @SuppressWarnings("serial")
    public static class RegexMissmatch extends ValidationError {
        public RegexMissmatch(String errorMessage) {
            super(errorMessage);
        }
    }

    private M owner;

    public Validator(M classOfT) {
        owner = classOfT;
    }

    @SuppressWarnings("unchecked")
    public <E> boolean unique(T instance, String key, E value) throws UniqueConstraintFailed, IOException {
        List<T> filtered = owner.filter(key, value);
        if (filtered.size() > 1) {
            throw new UniqueConstraintFailed(owner, key, value);
        } else if (filtered.size() == 1) {
            try {
                T object = filtered.get(0);
                Class<?> c = object.getClass();
                Field f = c.getDeclaredField(key);
                f.setAccessible(true);
                E foundValue = (E) f.get(object);
                if (instance.id == object.id && (foundValue == value || foundValue.equals(value)))
                    return true;
            } catch (Exception e) { }
            throw new UniqueConstraintFailed(owner, key, value);
        }
        return true;
    }

    public boolean email(String string) throws RegexMissmatch {
        if (string.matches("(\\w+)(\\.|_)?(\\w*)@(\\w+)(\\.(\\w+))+"))
            return true;
        throw new RegexMissmatch(string);
    }
}
