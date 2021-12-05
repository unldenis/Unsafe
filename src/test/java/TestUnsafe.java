import com.github.unldenis.unsafe.UnsafeUtil;

import java.util.Scanner;

public class TestUnsafe {

    public static void main(String[] args) throws InstantiationException {

        Scanner scanner = new Scanner(System.in);


        Scanner scannerCopy = UnsafeUtil.shallowCopy(scanner);
        String mex = scannerCopy.nextLine();

        System.out.println(mex);

    }
}
