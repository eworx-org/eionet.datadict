package eionet.datadict.dal;

import eionet.meta.service.DBUnitHelper;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@SpringApplicationContext("spring-context.xml")
public class VocabularyRepositoryTest extends UnitilsJUnit4 {

    @SpringBeanByType
    private VocabularyRepository vocabularyRepository;

    @Before
    public void setup() throws Exception {
        DBUnitHelper.loadData("seed-vocabulary-data.xml");
    }

    @Test
    public void existsByVocabularySetIdAndVocabularyIdentifier() {
        assertTrue(this.vocabularyRepository.exists(1, "test_vocabulary1"));
        assertFalse(this.vocabularyRepository.exists(101, "test_vocabulary1"));
        assertFalse(this.vocabularyRepository.exists(1, "test_vocabulary_not_here"));
    }

    @Test
    public void existsByVocabularySetIdentifierAndVocabularyIdentifier() {
        assertTrue(this.vocabularyRepository.exists("common", "test_vocabulary2"));
        assertFalse(this.vocabularyRepository.exists("common", "test_vocabulary102"));
        assertFalse(this.vocabularyRepository.exists("xxx", "test_vocabulary2"));
    }

}