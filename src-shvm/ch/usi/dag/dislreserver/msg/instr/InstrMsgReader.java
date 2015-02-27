package ch.usi.dag.dislreserver.msg.instr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InstrMsgReader {

    public static class InstrClassMessage {

        private byte[] control;
        private byte[] classCode;

        public InstrClassMessage(byte[] control, byte[] classCode) {
            this.control = control;
            this.classCode = classCode;
        }

        public byte[] getControl() {
            return control;
        }

        public byte[] getClassCode() {
            return classCode;
        }
    }

    public static InstrClassMessage readMessage(DataInputStream is) throws IOException {

        // protocol:
        // java int - control string length (ctl)
        // java int - class code length (ccl)
        // bytes[ctl] - control string (contains class name)
        // bytes[ccl] - class code

        int controlLength = is.readInt();
        int classCodeLength = is.readInt();

        // allocate buffer for class reading
        byte[] control = new byte[controlLength];
        byte[] classCode = new byte[classCodeLength];

        // read class
        is.readFully(control);
        is.readFully(classCode);

        return new InstrClassMessage(control, classCode);
    }

    public static void sendMessage(DataOutputStream os, InstrClassMessage icm)
            throws IOException {

        // protocol:
        // java int - control string (ctl)
        // java int - class code length (ccl)
        // bytes[ctl] - control string
        // bytes[ccl] - class code

        os.writeInt(icm.getControl().length);
        os.writeInt(icm.getClassCode().length);

        os.write(icm.getControl());
        os.write(icm.getClassCode());
        os.flush();
    }
}
