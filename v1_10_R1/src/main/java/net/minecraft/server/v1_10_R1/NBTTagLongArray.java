package net.minecraft.server.v1_10_R1;

import com.andavin.inject.InjectorVersion;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@InjectorVersion("1.0")
public class NBTTagLongArray extends NBTBase {

    private long[] b;

    NBTTagLongArray() {
    }

    public NBTTagLongArray(long[] along) {
        this.b = along;
    }

    public NBTTagLongArray(List<Long> list) {
        this(a(list));
    }

    @Override
    void write(DataOutput output) throws IOException {

        output.writeInt(this.b.length);
        long[] array = this.b;
        for (long k : array) {
            output.writeLong(k);
        }
    }

    @Override
    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {

        nbtreadlimiter.a(192L);
        int length = datainput.readInt();
        nbtreadlimiter.a((long) (64 * length));
        this.b = new long[length];
        for (int k = 0; k < length; k++) {
            this.b[k] = datainput.readLong();
        }
    }

    @Override
    public byte getTypeId() {
        return (byte) 12;
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("[L;");

        for (int i = 0; i < this.b.length; ++i) {

            if (i != 0) {
                stringbuilder.append(',');
            }

            stringbuilder.append(this.b[i]).append('L');
        }

        return stringbuilder.append(']').toString();
    }

    public NBTBase c() {
        long[] along = new long[this.b.length];
        System.arraycopy(this.b, 0, along, 0, this.b.length);
        return new NBTTagLongArray(along);
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object) && Arrays.equals(this.b, ((NBTTagLongArray) object).b);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(this.b);
    }

    @Override
    public NBTBase clone() {
        return this.c();
    }

    private static long[] a(List<Long> list) {

        long[] array = new long[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Long l = list.get(i);
            array[i] = l == null ? 0L : l;
        }

        return array;
    }
}
