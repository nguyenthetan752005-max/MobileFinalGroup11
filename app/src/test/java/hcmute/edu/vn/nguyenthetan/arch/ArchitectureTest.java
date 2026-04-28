package hcmute.edu.vn.nguyenthetan.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Architectural guard rails. These run as plain JUnit tests so a CI/dev
 * machine catches MVVM/layering regressions before they land.
 *
 * The two rules below were added together with the Đợt 2 refactor to make sure
 * future changes don't reintroduce direct Retrofit/Room calls inside Activities
 * or Fragments.
 */
public class ArchitectureTest {

    private static JavaClasses appClasses;

    @BeforeClass
    public static void importClasses() {
        appClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("hcmute.edu.vn.nguyenthetan");
    }

    @Test
    public void uiLayer_doesNotUseRetrofitApi() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ui..")
                .should().dependOnClassesThat().resideInAPackage("..data.remote.api..")
                .because("Activities/Fragments must go through ViewModel + UseCase, not Retrofit directly.");
        rule.check(appClasses);
    }

    @Test
    public void uiLayer_doesNotUseRoom() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ui..")
                .should().dependOnClassesThat().resideInAPackage("androidx.room..")
                .orShould().dependOnClassesThat().resideInAPackage("..data.local.dao..")
                .because("Activities/Fragments must go through ViewModel + UseCase, not Room DAOs directly.");
        rule.check(appClasses);
    }

    /* ── Đợt 5 additions ────────────────────────────────────────────── */

    /**
     * Guard against UI touching remote infra (mapper / sync / support).
     * NOTE: ui→data.remote.dto.* violations still exist (13 occurrences).
     * Those will be addressed in Đợt 7 by moving DTOs behind UseCases.
     */
    @Test
    public void uiLayer_doesNotUseRemoteInfra() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ui..")
                .should().dependOnClassesThat().resideInAPackage("..data.remote.mapper..")
                .orShould().dependOnClassesThat().resideInAPackage("..data.remote.sync..")
                .orShould().dependOnClassesThat().resideInAPackage("..data.remote.support..")
                .because("UI must not depend on remote infra classes (mapper/sync/support) — go through UseCase.");
        rule.check(appClasses);
    }

    @Test
    public void domainLayer_doesNotDependOnAndroid() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("android..")
                .because("domain layer should be pure Java without Android framework dependencies.");
        rule.check(appClasses);
    }

    @Test
    public void repositoryLayer_doesNotDependOnUi() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..data.repository..")
                .should().dependOnClassesThat().resideInAPackage("..ui..")
                .because("Data/Repository layer must never depend on the UI layer.");
        rule.check(appClasses);
    }
}
