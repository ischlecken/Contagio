package de.contagio.core.usecase

import de.contagio.core.domain.entity.Address
import de.contagio.core.domain.entity.Person
import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.entity.Teststation
import de.contagio.core.domain.port.IFindAllTester
import de.contagio.core.domain.port.IFindAllTeststation
import de.contagio.core.domain.port.PagedResult
import kotlin.test.Test
import kotlin.test.assertEquals

class ListOfAllTesterInfoTest {

    private fun <T> createPageRequest(t: T): PagedResult<T> {
        return PagedResult(
            content = listOf(t),
            pageSize = 10,
            totalPages = 1,
            totalElements = 1,
            isFirst = true,
            isLast = true
        )
    }

    @Test
    fun emptyTeststationEmptyTester_isEmpty() {
        val findAllTeststation = IFindAllTeststation {
            PagedResult()
        }

        val findAllTester = IFindAllTester {
            PagedResult()
        }

        val listOfAllTester = ListOfAllTesterInfo(findAllTeststation, findAllTester).execute()

        assertEquals(0, listOfAllTester.size)
    }

    @Test
    fun emptyTeststationWithOneTester_isEmpty() {
        val findAllTeststation = IFindAllTeststation {
            PagedResult()
        }

        val findAllTester = IFindAllTester {
            createPageRequest(
                Tester(
                    id = "1",
                    teststationId = "1",
                    person = Person(firstName = "hugo", lastName = "Blafasel")
                )
            )
        }

        val listOfAllTester = ListOfAllTesterInfo(findAllTeststation, findAllTester).execute()

        assertEquals(0, listOfAllTester.size)
    }

    @Test
    fun oneTeststationWithNotMatchingTester_isEmpty() {
        val findAllTeststation = IFindAllTeststation {
            createPageRequest(
                Teststation(
                    id = "0",
                    name = "teststation",
                    address = Address("blacity", "1234", "blastreet")
                )
            )
        }

        val findAllTester = IFindAllTester {
            createPageRequest(
                Tester(
                    id = "1",
                    teststationId = "1",
                    person = Person(firstName = "hugo", lastName = "Blafasel")
                )
            )
        }

        val listOfAllTester = ListOfAllTesterInfo(findAllTeststation, findAllTester).execute()

        assertEquals(0, listOfAllTester.size)
    }

    @Test
    fun oneTeststationWithMatchingTester_expectedDisplayname() {
        val findAllTeststation = IFindAllTeststation {
            createPageRequest(
                Teststation(
                    id = "1",
                    name = "teststation",
                    address = Address("blacity", "1234", "blastreet")
                )
            )
        }

        val findAllTester = IFindAllTester {
            createPageRequest(
                Tester(
                    id = "1",
                    teststationId = "1",
                    person = Person(firstName = "hugo", lastName = "Blafasel")
                )
            )
        }

        val listOfAllTester = ListOfAllTesterInfo(findAllTeststation, findAllTester).execute()

        assertEquals(1, listOfAllTester.size)
        assertEquals("hugo Blafasel in teststation", listOfAllTester["1"]?.displayInfo)
    }
}
